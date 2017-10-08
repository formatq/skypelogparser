package org.formatq;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
    static SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    static String separator = "~";

    public static void main(String[] args) throws IOException {
        Path path = null;
        String filepath = "";
        if (args.length == 1) {
            filepath = args[0];
            path = Paths.get(filepath);
            boolean exists = Files.exists(path, LinkOption.NOFOLLOW_LINKS);
            if (!exists) {
                log("File with path '" + filepath + "' does not exists.");
                return;
            } else {
                if (!path.toString().endsWith(".csv")) {
                    log("Please, enter path to *.csv file.");
                    return;
                }
            }
        } else {
            log("No file path. Please, enter path of the csv file.");
            return;
        }
        log("Parsing started.");
        String header = "ConversationId,ConversationName,AuthorId,AuthorName,HumanTime,Date,DateTime,CallTime,ContentXml";
        String flag = "\"--?T::Z\",";

        //path = Paths.get("SkypeChatHistory.csv");
        byte[] bytes = Files.readAllBytes(path);
        String s = new String(bytes, StandardCharsets.UTF_8);
        int i = s.indexOf("\r\n");
        String substring = s.substring(i + 2, s.length() - i);

        List<String> strings = new ArrayList<>();
        StringBuilder temp = new StringBuilder();
        char[] chars = substring.toCharArray();
        boolean quotOpen = false;
        for (int j = 0; j < chars.length; j++) {
            if (chars[j] == '"') {
                quotOpen = !quotOpen;
            }

            if (chars[j] == '\r') {
                if (quotOpen) {
                    temp.append(chars[j]);
                    continue;
                }
                String replace = temp.toString().replace("\r\n", "").replace("\r", "").replace("\n", "");
                String withDates = addDates(replace, flag);
                //change separator from ',' to another
                String changeSeparator = changeSeparator(withDates);
                strings.add(changeSeparator);
                temp = new StringBuilder();
                j++;
            } else {
                temp.append(chars[j]);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(header.replace(",", separator)).append("\r\n");
        for (String string : strings) {
            stringBuilder.append(string).append("\r\n");

        }
        String newFile = filepath.replace(".csv", " - handled.csv");
        Files.write(Paths.get(newFile), stringBuilder.toString().getBytes(), StandardOpenOption.CREATE);
        //System.out.println(out);
        log("Parsing completed. New file is '" + newFile + "'");
    }

    private static String changeSeparator(String row) {
        StringBuilder result = new StringBuilder();
        char[] rowChars = row.toCharArray();
        boolean quotOpen = false;
        for (char rowChar : rowChars) {
            if (rowChar == '"') {
                quotOpen = !quotOpen;
            }

            if (rowChar == ',') {
                if (quotOpen) {
                    result.append(rowChar);
                    continue;
                }
                result.append(separator);
            } else {
                result.append(rowChar);
            }
        }
        return result.toString();
    }


    public static String addDates(String str, String flag) {
        int i = str.indexOf(flag);
        int start = i + flag.length();
        int end = start + 13; //timespan lenght 1507215307141

        String timespanStr = str.substring(start, end);
        long timespanLong = Long.parseLong(timespanStr);
        Date date = new Date(timespanLong);
        String format = simpleDateFormat.format(date);
        String format2 = simpleDateFormat2.format(date);

        String newDateColumns = format + "," + format2;

        String strBegin = str.substring(0, start);
        String strEnd = str.substring(end, str.length());

        String min = ",";
        //<partlist type=""started"" alt="""">  <part identity=""ayu_ivanov"">    <name>Alexander Ivanov</name>    <duration>158</duration>  </part>  <part identity=""stalf84"">    <name>Котов Сергей (П)</name>    <duration>158</duration>  </part></partlist>
        Pattern pattern = Pattern.compile("(?!(<duration>))[0-9]{1,8}(?=(</duration>))");
        Matcher matcher = pattern.matcher(strEnd);
        if (matcher.find()) {
            String group = matcher.group();
            double minutes = Double.parseDouble(group) / 60;
            min = min + String.valueOf(Math.round(minutes));
        }


        return strBegin + newDateColumns + min + strEnd;
    }

    public static void log(String s) {
        System.out.println(s);
    }

}
