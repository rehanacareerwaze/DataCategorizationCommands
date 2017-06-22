import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class ApacheCsvParser {
    PrintStream out;

    public void parse(String inFileName, String outFileName)
            throws IOException {
        out = new PrintStream(new FileOutputStream(outFileName));

        Reader in = new FileReader(inFileName);
        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
        String lastDomain = null;
        String lastSubDomain = null;

        for (CSVRecord record : records) {
            String domain = record.get(0);
            String subDomain = record.get(1);
            String skill = record.get(2);

            if (!isBlank(domain) && !isBlank(subDomain) && !isBlank(skill)) {
                // Heading.  Ignore.
                continue;
            }

            if (!isBlank(domain)) {
                printDomain(domain);
                lastDomain = domain;
            } else if (!isBlank(subDomain)) {
                printSubDomain(lastDomain, subDomain);
                lastSubDomain = subDomain;
            }  else if (!isBlank(skill)) {
                printSkill(lastSubDomain, skill);
            }
        }
        in.close();

    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void printDomain(String domain) {
        out.printf("CREATE (n:Domain { name : '%s', type : 'Technology', "
                + "visible : 'True'})\n", domain);
        out.println("RETURN n\n");
    }

    private void printSubDomain(String domain, String subDomain) {
        out.printf("CREATE (n:SubDomain { name : '%s', type : 'Technology', "
                + "visible : 'True'})\n", subDomain);
        out.println("RETURN n\n");
        out.println("MATCH (a:Domain),(b:SubDomain)");
        out.printf("WHERE a.name = '%s' AND b.name = '%s'\n", domain,
                subDomain);
        out.println("CREATE (a)-[r:SubDomain]->(b)");
        out.println("RETURN r\n");
    }

    private void printSkill(String subDomain, String skills) {
        out.printf("CREATE (n:Skill { name : '%s', type : 'Software Package', "
                + "visible : 'True'})\n", subDomain);
        out.println("RETURN n\n");
        out.println("MATCH (a:SubDomain),(b:Skill)");
        out.printf("WHERE a.name = '%s' AND b.name = '%s'\n", subDomain,
                skills);
        out.println("CREATE (a)-[r:Skill]->(b)");
        out.println("RETURN r\n");
    }

    public static void main(String[] args)
            throws IOException {
        ApacheCsvParser parser = new ApacheCsvParser();
        parser.parse(args[0], args[1]);
    }
}
