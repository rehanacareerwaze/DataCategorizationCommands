import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class Neo4jParser {
    private static final int DOMAIN_FIELD = 0;
    private static final int SUBDOMAIN_FIELD = 1;
    private static final int SKILL_FIELD = 2;

    BufferedReader in;
    PrintStream out;

    public void parse(String inFileName, String outFileName)
            throws IOException {
        in = new BufferedReader(new FileReader(inFileName));
        out = new PrintStream(new FileOutputStream(outFileName));

        String line;
        String domain = null;
        String subDomain = null;
        String skill = null;
        while ((line = in.readLine()) != null) {
            line += ",DUMMY";
            String[] fields = line.split(",");
            if (!fields[DOMAIN_FIELD].isEmpty() && !fields[SUBDOMAIN_FIELD]
                    .isEmpty()) {
                // Heading row.  Ignore.
                continue;
            }
            if (fields[DOMAIN_FIELD].isEmpty() && fields[SUBDOMAIN_FIELD]
                    .isEmpty() && fields[SKILL_FIELD].isEmpty()) {
                // Blank row.  Ignore.
                continue;
            }

            if (!fields[DOMAIN_FIELD].isEmpty()) {
                // Domain row.
                domain = fields[DOMAIN_FIELD];
                printDomain(domain);
            }
            else if (!fields[SUBDOMAIN_FIELD].isEmpty()) {
                // Subdomain row.
                subDomain = fields[SUBDOMAIN_FIELD];
                printSubDomain(domain, subDomain);
            }
            else if (!fields[SUBDOMAIN_FIELD].isEmpty()) {
                // Skill row
                skill = fields[SKILL_FIELD];
                printSkill(subDomain, skill);
            }

        }
        in.close();
        out.close();
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

    private void printSkill(String subDomain, String skill) {
        out.printf("CREATE (n:Skill { name : '%s', type : 'Software Package', "
                + "visible : 'True'})\n", subDomain);
        out.println("RETURN n\n");
        out.println("MATCH (a:SubDomain),(b:Skill)");
        out.printf("WHERE a.name = '%s' AND b.name = '%s'\n", subDomain, skill);
        out.println("CREATE (a)-[r:Skill]->(b)");
        out.println("RETURN r\n");
    }

    public static void main(String[] args)
            throws IOException {
        Neo4jParser parser = new Neo4jParser();
        parser.parse(args[0], args[1]);
    }
}