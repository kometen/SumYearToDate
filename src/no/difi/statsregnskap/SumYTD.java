package no.difi.statsregnskap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.NumberFormatException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multiset;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

public class SumYTD {

    private static String FNAME = "/home/claus/data/statsregnskap/statsregnskapet_full_historikk-utf8-formatted.csv";
    //private static final String FNAME = "/Users/claus/data/statsregnskap/statsregnskapet_hittil_i_aar-utf8-formatted.csv";

    private static final String HEADING = "År;Periode;Konto_no;Konto;Programområde_id;Programområde;Programkategori_id;" +
            "Programkategori;Fagdepartement_id;Fagdepartement;Kapittel_id;Kapittel;Post_id;Post;Post_type;Kontoklasse_id;" +
            "Kontoklasse;Kontogruppe_id;Kontogruppe;Artskonto_id;Artskonto;Fagdepartement_Virksomhet_id;" +
            "Fagdepartement_Virksomhet;Virksomhet_id;Virksomhet;Regnskapsfører_id;Regnskapsfører;Beløp;Hittil_i_år";

    public static void main(String[] args) {

        if (args.length > 0) {
            FNAME = args[0];
        }

        try (BufferedReader br = new BufferedReader(new FileReader(FNAME))) {

            String currentLine, map_key, map_value, accounting_line_string, ytd_primary_key, ytd_secondary_key;
            String[] parts;
            LinkedHashMap<String, BigDecimal> merge_duplicate_lines_map = new LinkedHashMap<String, BigDecimal>();
            BigDecimal map_value_exist;

            // Read file and extract last element. If line exist sum values, otherwise add it to hashmap.
            while ((currentLine = br.readLine()) != null) {
                int lastIndexOf = currentLine.lastIndexOf(';');
                //System.out.println(currentLine.substring(0, lastIndexOf));
                //System.out.println(currentLine.substring(lastIndexOf + 1));
                map_key = currentLine.substring(0, lastIndexOf);
                map_value = currentLine.substring(lastIndexOf + 1);
                map_value_exist = merge_duplicate_lines_map.get(map_key);
                if (map_value_exist != null) {
                    BigDecimal sum = map_value_exist.add(new BigDecimal(map_value));
                    merge_duplicate_lines_map.put(map_key, sum);
                } else {
                    try {
                        merge_duplicate_lines_map.put(map_key, new BigDecimal(map_value));
                    } catch (NumberFormatException e) {
                        System.err.println(e);
                    }
                }
            }

            //System.out.println("Map-entries: " + merge_duplicate_lines_map.size());

            BigDecimal ytd = new BigDecimal("0.0"); // Year To Date sum.
            Table<String, String, AccountingLine> ytd_sum_map = TreeBasedTable.create();
            // Loop hashmap.
            // Add accounting records to guava-table using unique identifiers.
            // Traverse the table to sum YTD (Year To Date).
            Set set = merge_duplicate_lines_map.entrySet();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry mentry = (Map.Entry)iterator.next();
                accounting_line_string = mentry.getKey().toString() + ";" + mentry.getValue().toString();
                //System.out.println(accounting_line_string);
                // Split accounting line so we can extract the parts that makes up a unique line.
                parts = accounting_line_string.split(";");
                // Primary key may differ depending on the number of fields in CSV-file. The reason is the file
                // comes in two versions. One with the complete records (28 parts) and additions (17 parts).
                //System.out.println(parts.length);
                if (parts.length == 17) {
                    ytd_primary_key = parts[0] + ";" + parts[8] + ";" + parts[12] + ";" + parts[14] + ";" + parts[10];
                    ytd_secondary_key = parts[1];
                    // Add empty values so it corresponds to the complete CSV when it contains more fields.
                    accounting_line_string =  parts[0]  + ";" + parts[1]  + ";" + parts[2]  + ";" + parts[3]  + ";;;;;;;;;;;;";
                    accounting_line_string += parts[4]  + ";" + parts[5]  + ";" + parts[6]  + ";" + parts[7]  + ";" + parts[8] + ";";
                    accounting_line_string += parts[9]  + ";" + parts[10] + ";" + parts[11] + ";" + parts[12] + ";";
                    accounting_line_string += parts[13] + ";" + parts[14] + ";" + parts[15] + ";" + parts[16];
                } else {
                    ytd_primary_key = parts[0] + ";" + parts[8] + ";" + parts[12] + ";" + parts[19] + ";" + parts[23] + ";" + parts[25] + ";" + parts[21];
                    ytd_secondary_key = parts[1];
                }
                //System.out.println(ytd_primary_key);
                AccountingLine accountingLine = new AccountingLine(accounting_line_string, (BigDecimal) mentry.getValue());
                ytd_sum_map.put(ytd_primary_key, ytd_secondary_key, accountingLine);
                //System.out.println(accounting_line_string.toString());
            }

            // Print header.
            System.out.println(HEADING);
            // Loop guava-table and sum YTD.
            for (String item : ytd_sum_map.rowKeySet()) {
                ytd = BigDecimal.ZERO;
                for (Map.Entry<String, AccountingLine> period : ytd_sum_map.row(item).entrySet()) {
                    ytd = ytd.add(period.getValue().getSum());
                    System.out.println(period.getValue().getAccountingLine() + ";" + ytd.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
