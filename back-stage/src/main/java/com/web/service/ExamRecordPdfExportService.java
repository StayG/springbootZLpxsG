package com.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.web.domain.Resp.ExamDetailsResp;
import com.web.domain.Resp.ExamRecordResp;
import com.web.utils.ExamStatusConstants;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 导出「考试信息 + 学生作答 + 批改结果 + 标准答案与解析」PDF
 */
@Service
public class ExamRecordPdfExportService {

    private static final ObjectMapper JSON = new ObjectMapper();

    private static int typePriority(Integer examQuestionTypes) {
        int t = examQuestionTypes == null ? 0 : examQuestionTypes;
        return switch (t) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> 4;
            case 5 -> 5;
            default -> 99;
        };
    }

    private static int typeOrder(ExamDetailsResp d) {
        return typePriority(d.getExamQuestionTypes());
    }

    private static int sequence(ExamDetailsResp d) {
        Integer s = d.getExamPaperTopicSequence();
        return s == null ? Integer.MAX_VALUE : s;
    }

    private static String plainText(String html) {
        if (html == null) {
            return "";
        }
        String s = html.replaceAll("(?i)<script[\\s\\S]*?</script>", " ");
        s = s.replaceAll("<[^>]+>", " ");
        s = s.replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
        s = s.replaceAll("\\s+", " ").trim();
        return s;
    }

    private static List<OptionRow> parseOptions(String json) {
        if (json == null || json.isBlank() || "[]".equals(json.trim())) {
            return List.of();
        }
        try {
            JsonNode root = JSON.readTree(json);
            if (!root.isArray()) {
                return List.of();
            }
            List<OptionRow> out = new ArrayList<>();
            int idx = 0;
            for (JsonNode n : root) {
                if (n.isObject() && n.has("code") && n.has("text")) {
                    out.add(new OptionRow(n.get("code").asText(""), plainText(n.get("text").asText(""))));
                } else if (n.isTextual()) {
                    String raw = n.asText("");
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("^([A-Za-z])\\.\\s*(.*)$").matcher(raw);
                    if (m.find()) {
                        out.add(new OptionRow(m.group(1), m.group(2).trim()));
                    } else {
                        char code = (char) ('A' + idx);
                        out.add(new OptionRow(String.valueOf(code), raw.trim()));
                    }
                }
                idx++;
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    private static String formatChoiceAnswer(ExamDetailsResp item, String codesRaw) {
        if (codesRaw == null || codesRaw.isBlank() || "未作答".equals(codesRaw.trim())) {
            return "";
        }
        String raw = codesRaw.trim();
        int t = item.getExamQuestionTypes() == null ? 0 : item.getExamQuestionTypes();
        List<OptionRow> opts = parseOptions(item.getExamQuestionOptions());
        if (opts.isEmpty()) {
            return raw;
        }
        java.util.Map<String, String> map = opts.stream().collect(Collectors.toMap(o -> o.code.trim(), o -> o.text, (a, b) -> a));
        List<String> parts = (t == 2)
                ? java.util.Arrays.stream(raw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList())
                : List.of(raw);
        return parts.stream()
                .map(code -> {
                    String text = map.get(code);
                    return text != null && !text.isEmpty() ? (code + ". " + text) : code;
                })
                .collect(Collectors.joining("；"));
    }

    private static String myAnswerText(ExamDetailsResp item) {
        String my = item.getExamDetailsMyanswer();
        if (my == null || my.isBlank() || "未作答".equals(my.trim())) {
            return "未作答";
        }
        int t = item.getExamQuestionTypes() == null ? 0 : item.getExamQuestionTypes();
        if (t == 1 || t == 2 || t == 3) {
            String s = formatChoiceAnswer(item, my);
            return s.isEmpty() ? my.trim() : s;
        }
        return my.trim();
    }

    private static String referenceAnswerText(ExamDetailsResp item) {
        if (item.getExamQuestionAnswer() == null || item.getExamQuestionAnswer().isBlank()) {
            return "—";
        }
        int t = item.getExamQuestionTypes() == null ? 0 : item.getExamQuestionTypes();
        if (t == 1 || t == 2 || t == 3) {
            String s = formatChoiceAnswer(item, item.getExamQuestionAnswer().trim());
            return s.isEmpty() ? item.getExamQuestionAnswer().trim() : s;
        }
        return item.getExamQuestionAnswer().trim();
    }

    private static String scoreLine(ExamDetailsResp item) {
        Integer sc = item.getExamDetailsMyscore();
        String base = (sc == null ? "—" : (sc + " 分"));
        int t = item.getExamQuestionTypes() == null ? 0 : item.getExamQuestionTypes();
        if (t == 5) {
            Integer ts = item.getTeacherScore();
            String c = item.getTeacherComment();
            StringBuilder sb = new StringBuilder(base);
            if (ts != null) {
                sb.append("（教师评分 ").append(ts).append(" 分）");
            }
            if (c != null && !c.isBlank()) {
                sb.append(" 评语：").append(c.trim());
            }
            Integer rs = item.getReviewStatus();
            if (rs == null || rs == 0) {
                sb.append(" 【待批阅】");
            }
            return sb.toString();
        }
        return base;
    }

    /**
     * 是否允许导出含标准答案与批改结果的完整 PDF（与前端「已发布」一致）
     */
    public static boolean isExportable(ExamRecordResp resp, List<ExamDetailsResp> details) {
        if (resp == null || details == null) {
            return false;
        }
        Integer st = resp.getStatus();
        if (st == null || st == ExamStatusConstants.RECORD_IN_PROGRESS) {
            return false;
        }
        for (ExamDetailsResp d : details) {
            Integer t = d.getExamQuestionTypes();
            if (t != null && t == 5) {
                Integer rs = d.getReviewStatus();
                if (rs == null || rs == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public byte[] buildFullAnswerPdf(ExamRecordResp resp) throws DocumentException, IOException {
        List<ExamDetailsResp> raw = resp.getExamDetailsRespList();
        if (raw == null) {
            raw = List.of();
        }
        List<ExamDetailsResp> list = new ArrayList<>(raw);
        list.sort(Comparator
                .comparingInt(ExamRecordPdfExportService::typeOrder)
                .thenComparingInt(ExamRecordPdfExportService::sequence));

        BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font titleFont = new Font(bf, 16, Font.BOLD, BaseColor.BLACK);
        Font headFont = new Font(bf, 12, Font.BOLD, BaseColor.BLACK);
        Font bodyFont = new Font(bf, 10.5f, Font.NORMAL, BaseColor.BLACK);
        Font smallFont = new Font(bf, 9.5f, Font.NORMAL, BaseColor.DARK_GRAY);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

        Document document = new Document(PageSize.A4, 48, 48, 56, 56);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        Paragraph pTitle = new Paragraph("【成绩导出】完整答卷", titleFont);
        pTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(pTitle);
        document.add(new Paragraph(" "));

        String examTitle = firstNonBlank(resp.getExamName(), resp.getExamPaperName(), "考试");
        String studentName = firstNonBlank(resp.getNickname(), "学生");
        document.add(new Paragraph("考试名称：" + examTitle, headFont));
        document.add(new Paragraph("学生姓名：" + studentName, headFont));
        document.add(new Paragraph(" "));

        String scoreStr = resp.getTotalScore() == null ? "—" : String.valueOf(resp.getTotalScore());
        String fullStr = resp.getExamPaperMyscore() == null ? "—" : String.valueOf(resp.getExamPaperMyscore());
        document.add(new Paragraph("得分 / 总分：" + scoreStr + " / " + fullStr, bodyFont));
        if (resp.getExamRank() != null && resp.getExamRankTotal() != null && resp.getExamRankTotal() > 0) {
            document.add(new Paragraph("本场排名：" + resp.getExamRank() + " / " + resp.getExamRankTotal(), bodyFont));
        }
        document.add(new Paragraph("考试安排时间：" + fmtRange(sdf, resp.getExamScheduleStart(), resp.getExamScheduleEnd()), bodyFont));
        document.add(new Paragraph("进入考试时间：" + fmt(sdf, resp.getInsertTime()), bodyFont));
        document.add(new Paragraph("交卷时间：" + fmt(sdf, resp.getEndTime()), bodyFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("────────────────────────────────────────", smallFont));
        document.add(new Paragraph(" "));

        int n = 1;
        for (ExamDetailsResp item : list) {
            String typeName = firstNonBlank(item.getExamQuestionTypesName(), "试题");
            Integer full = item.getExamPaperTopicNumber();
            String fullPart = full == null ? "" : ("（满分 " + full + " 分）");

            document.add(new Paragraph("第 " + n + " 题【" + typeName + "】" + fullPart, headFont));
            document.add(new Paragraph("题干：" + plainText(item.getExamQuestionName()), bodyFont));

            List<OptionRow> opts = parseOptions(item.getExamQuestionOptions());
            if (!opts.isEmpty()) {
                StringBuilder ob = new StringBuilder("选项：");
                for (OptionRow o : opts) {
                    ob.append("\n").append(o.code).append(". ").append(o.text);
                }
                document.add(new Paragraph(ob.toString(), bodyFont));
            }

            document.add(new Paragraph("我的作答：" + myAnswerText(item), bodyFont));
            document.add(new Paragraph("批改结果：" + scoreLine(item), bodyFont));
            document.add(new Paragraph("标准答案：" + referenceAnswerText(item), bodyFont));
            String ana = item.getExamQuestionAnalysis();
            document.add(new Paragraph("解析：" + (ana == null || ana.isBlank() ? "—" : plainText(ana)), bodyFont));
            document.add(new Paragraph(" "));
            n++;
        }

        document.close();
        return baos.toByteArray();
    }

    private static String fmt(SimpleDateFormat sdf, Date d) {
        if (d == null) {
            return "—";
        }
        return sdf.format(d);
    }

    private static String fmtRange(SimpleDateFormat sdf, Date a, Date b) {
        String x = a == null ? "—" : sdf.format(a);
        String y = b == null ? "—" : sdf.format(b);
        return x + " — " + y;
    }

    private static String firstNonBlank(String... xs) {
        for (String x : xs) {
            if (x != null && !x.isBlank()) {
                return x.trim();
            }
        }
        return "";
    }

    private record OptionRow(String code, String text) {
    }
}
