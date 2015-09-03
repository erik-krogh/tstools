package dk.webbies.tscreate.paser;


import java.util.List;

/**
 * Created by erik1 on 01-09-2015.
 */
public class Program extends Statement {
    private List<Statement> body;

    public Program(int line, List<Statement> body) {
        super(line);
        this.body = body;
    }


    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public List<Statement> getBody() {
        return body;
    }
}
