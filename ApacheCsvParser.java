import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class ApacheCsvParser {
    PrintStream out;

    private String domainString;
    private String domainVar;
    private String subDomainString;
    private String subDomainVar;
    private String skillString;
    private String skillVar;

    public void parse(String inFileName, String outFileName)
            throws IOException {

        out = new PrintStream(new FileOutputStream(outFileName));

        Reader in = new FileReader(inFileName);
        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);

        for (CSVRecord record : records) {
            String domain = record.get(0);
            String subDomain = record.get(1);
            String skill = record.get(2);


            if (!isBlank(domain) && !isBlank(subDomain) && !isBlank(skill)) {
                // Heading.  Ignore.
                continue;
            }

            if (!isBlank(domain)) {
                if (!isBlank(domainVar)) {
                    printMatch();
                }
                domainString = domain;
                domainVar = valueToName(domain);
                printDomain();
            } else if (!isBlank(subDomain)) {
                subDomainString = subDomain;
                subDomainVar = valueToName(subDomain);
                printSubDomain();
            }  else if (!isBlank(skill)) {
                skillString = skill;
                skillVar = valueToName(skill);
                printSkill();
            }
        }
        if (!isBlank(domainVar)) {
            printMatch();
        }

        in.close();

    }

    private boolean isBlank(String s) {
        if (s == null) {
            return true;
        }
        String trimmed = s.trim();
        return  trimmed.isEmpty() || trimmed.equals("-");
    }

    private void printDomain() {
        out.printf("\nCREATE (%s:Domain { name : '%s', type : 'Technology', "
                + "visible : 'True'}),\n", domainVar, domainString);
    }

    private void printSubDomain() {
        out.printf("\nCREATE (%s:SubDomain { name : '%s', type : 'Technology', "
                + "visible : 'True'}),\n", subDomainVar, subDomainString);
        out.printf("(%s)-[:SubDomain]->(%s),\n", domainVar, subDomainVar);
    }

    private void printSkill() {
        out.printf("(%s:Skill { name : '%s'}),\n", skillVar, skillString);
        out.printf("(%s)-[:Skill]->(%s),\n", subDomainVar, skillVar);
    }

    private void printMatch()  {
        out.printf("\nMATCH (domain:Domain {name: '%s'})\n", domainString);
        out.println("OPTIONAL MATCH (domain)-[:SubDomain]->(subdomain:SubDomain)");
        out.println("OPTIONAL MATCH (subdomain)-[:Skill]->(skill:Skill)");
        out.println("RETURN domain, subdomain, skill;");

    }

    private String valueToName(String s) {
        return s.replaceAll("[^a-zA-Z0-9]+", "");
    }

    public static void main(String[] args)
            throws IOException {
        ApacheCsvParser parser = new ApacheCsvParser();
        parser.parse(args[0], args[1]);
    }
}


