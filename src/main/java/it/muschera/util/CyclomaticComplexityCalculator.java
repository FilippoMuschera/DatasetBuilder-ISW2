package it.muschera.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import it.muschera.model.JavaClass;

public class CyclomaticComplexityCalculator {

    private CyclomaticComplexityCalculator() {
        //solo metodi statici
    }

    public static int calculate(JavaClass javaClass) {
        // Parse the input string using JavaParser
        JavaParser jp = new JavaParser();
        ParseResult<CompilationUnit> parseResult = jp.parse(javaClass.getContent());
        CompilationUnit compilationUnit;

        compilationUnit = parseResult.getResult().orElseThrow();


        // Create a visitor to traverse the AST and calculate the cyclomatic complexity
        CyclomaticComplexityVisitor visitor = new CyclomaticComplexityVisitor();
        visitor.visit(compilationUnit, null);

        // Return the calculated cyclomatic complexity
        return visitor.getCyclomaticComplexity();
    }

    private static class CyclomaticComplexityVisitor extends VoidVisitorAdapter<Void> {
        private int cyclomaticComplexity = 1; // Start with 1 for the default path

        @Override
        public void visit(IfStmt n, Void arg) {
            cyclomaticComplexity++;
            super.visit(n, arg);
        }

        @Override
        public void visit(WhileStmt n, Void arg) {
            cyclomaticComplexity++;
            super.visit(n, arg);
        }

        @Override
        public void visit(DoStmt n, Void arg) {
            cyclomaticComplexity++;
            super.visit(n, arg);
        }

        @Override
        public void visit(ForStmt n, Void arg) {
            cyclomaticComplexity++;
            super.visit(n, arg);
        }

        @Override
        public void visit(SwitchStmt n, Void arg) {
            cyclomaticComplexity++;
            super.visit(n, arg);
        }

        public int getCyclomaticComplexity() {
            return cyclomaticComplexity;
        }
    }
}