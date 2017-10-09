package org.fmq.skype.logparser;

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

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static String columnSeparator = "~";
    private static String rowSeparator = "\r\n";

    private static String header = "ConversationId,ConversationName,AuthorId,AuthorName,HumanTime,Date,DateTime,CallTime,ContentXml";
    private static String flag = "\"--?T::Z\",";


    public static void main(String[] args) throws IOException {
        Path path;
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
        String newFile = parse(path);
        log("Parsing completed. New file is '" + newFile + "'");
    }

    private static String parse(Path path) throws IOException {
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
                String replace = temp.toString().replace(rowSeparator, "").replace("\r", "").replace("\n", "");
                String withDates = addNewColumns(replace, flag);
                String changeSeparator = changeSeparator(withDates);
                strings.add(changeSeparator);
                temp = new StringBuilder();
                j++;
            } else {
                temp.append(chars[j]);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(header.replace(",", columnSeparator)).append(rowSeparator);
        for (String string : strings) {
            stringBuilder.append(string).append(rowSeparator);

        }
        String newFile = path.toString().replace(".csv", " - handled.csv");
        Files.write(Paths.get(newFile), stringBuilder.toString().getBytes(), StandardOpenOption.CREATE);
        return newFile;
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
                result.append(columnSeparator);
            } else {
                result.append(rowChar);
            }
        }
        return result.toString();
    }


    private static String addNewColumns(String str, String flag) {
        int i = str.indexOf(flag);
        int start = i + flag.length();
        int end = start + 13; //timespan lenght 1507215307141

        // adding dates
        String timespanStr = str.substring(start, end);
        long timespanLong = Long.parseLong(timespanStr);
        Date date = new Date(timespanLong);
        String format = simpleDateFormat.format(date);
        String format2 = simpleDateFormat2.format(date);

        String newDateColumns = format + "," + format2;

        String strBegin = str.substring(0, start);
        String strEnd = str.substring(end, str.length());

        //adding conversation time
        String min = ",";

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
