package org.Lcing.snowstorm_engine.molang;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple Recursive Descent Parser for Molang.
 * Parses strings like "variable.age * 0.5 + 1" into an IMolangExpression tree.
 */
public class MolangParser {

    public static IMolangExpression parse(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return IMolangExpression.ZERO;
        }
        // Very basic tokenizer: split by known delimiters but keep them?
        // For simplicity, we'll process char by char in a dedicated cursor class.
        return new State(expression).parseExpression();
    }

    public static IMolangExpression parseJson(com.google.gson.JsonElement element) {
        if (element == null)
            return IMolangExpression.ZERO;
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return IMolangExpression.constant(element.getAsFloat());
        }
        return parse(element.getAsString());
    }

    private static class State {
        private final String src;
        private int pos = 0;

        public State(String src) {
            this.src = src;
        }

        private int peek() {
            if (pos >= src.length())
                return -1;
            return src.charAt(pos);
        }

        private int next() {
            int c = peek();
            if (c != -1)
                pos++;
            return c;
        }

        private boolean consume(char expected) {
            skipWhitespace();
            if (peek() == expected) {
                pos++;
                return true;
            }
            return false;
        }

        private void skipWhitespace() {
            while (peek() != -1 && Character.isWhitespace(peek())) {
                pos++;
            }
        }

        // Expression -> Term { (+|-) Term }
        public IMolangExpression parseExpression() {
            IMolangExpression left = parseTerm();

            while (true) {
                skipWhitespace();
                int c = peek();
                if (c == '+') {
                    consume('+');
                    IMolangExpression right = parseTerm();
                    left = new MolangExpressions.BinaryOp(left, right, MolangExpressions.BinaryOp.OpType.ADD);
                } else if (c == '-') {
                    consume('-');
                    IMolangExpression right = parseTerm();
                    left = new MolangExpressions.BinaryOp(left, right, MolangExpressions.BinaryOp.OpType.SUBTRACT);
                } else {
                    break;
                }
            }
            return left;
        }

        // Term -> Factor { (*|/) Factor }
        private IMolangExpression parseTerm() {
            IMolangExpression left = parseFactor();

            while (true) {
                skipWhitespace();
                int c = peek();
                if (c == '*') {
                    consume('*');
                    IMolangExpression right = parseFactor();
                    left = new MolangExpressions.BinaryOp(left, right, MolangExpressions.BinaryOp.OpType.MULTIPLY);
                } else if (c == '/') {
                    consume('/');
                    IMolangExpression right = parseFactor();
                    left = new MolangExpressions.BinaryOp(left, right, MolangExpressions.BinaryOp.OpType.DIVIDE);
                } else {
                    break;
                }
            }
            return left;
        }

        // Factor -> Number | Variable | Function | ( Expression ) | -Factor (unary
        // minus)
        private IMolangExpression parseFactor() {
            skipWhitespace();
            int c = peek();

            // Parentheses
            if (consume('(')) {
                IMolangExpression expr = parseExpression();
                consume(')');
                return expr;
            }

            // Unary minus: check if '-' is followed by a letter (function/variable)
            // or another '(' (expression)
            if (c == '-') {
                int nextPos = pos + 1;
                if (nextPos < src.length()) {
                    char nextChar = src.charAt(nextPos);
                    // If next char is letter or '(', treat as unary minus
                    if (Character.isLetter(nextChar) || nextChar == '(') {
                        next(); // consume the '-'
                        IMolangExpression inner = parseFactor();
                        return new MolangExpressions.UnaryMinus(inner);
                    }
                }
                // Otherwise fall through to parseNumber for negative numbers like -5.0
            }

            // Number
            if (Character.isDigit(c) || c == '.' || c == '-') {
                return parseNumber();
            }

            // Identifier (Variable or Function)
            if (Character.isLetter(c) || c == '_') {
                return parseIdentifier();
            }

            // Default fallback
            next(); // consume invalid char
            return IMolangExpression.ZERO;
        }

        private IMolangExpression parseIdentifier() {
            StringBuilder sb = new StringBuilder();
            while (true) {
                int c = peek();
                if (Character.isLetterOrDigit(c) || c == '_' || c == '.') {
                    sb.append((char) next());
                } else {
                    break;
                }
            }
            String name = sb.toString();

            // Expand Bedrock variable shorthands
            if (name.startsWith("v.")) {
                name = "variable." + name.substring(2);
            } else if (name.startsWith("q.")) {
                name = "query." + name.substring(2);
            } else if (name.startsWith("t.")) {
                name = "temp." + name.substring(2);
            } else if (name.startsWith("c.")) {
                name = "context." + name.substring(2);
            }

            skipWhitespace();
            // Function call?
            if (peek() == '(') {
                return parseFunctionCall(name);
            }

            return new MolangExpressions.Variable(name);
        }

        private IMolangExpression parseFunctionCall(String funcName) {
            consume('(');
            List<IMolangExpression> args = new ArrayList<>();
            if (peek() != ')') {
                args.add(parseExpression());
                while (consume(',')) {
                    args.add(parseExpression());
                }
            }
            consume(')');

            // Case-insensitive matching (Bedrock allows Math.random and math.random)
            String fn = funcName.toLowerCase();

            if (fn.equals("math.random") && args.size() >= 2) {
                return new MolangExpressions.MathRandom(args.get(0), args.get(1));
            }
            if (fn.equals("math.sin") && args.size() >= 1) {
                return new MolangExpressions.MathSin(args.get(0));
            }
            if (fn.equals("math.cos") && args.size() >= 1) {
                return new MolangExpressions.MathCos(args.get(0));
            }
            if (fn.equals("math.abs") && args.size() >= 1) {
                return new MolangExpressions.MathAbs(args.get(0));
            }
            if (fn.equals("math.clamp") && args.size() >= 3) {
                return new MolangExpressions.MathClamp(args.get(0), args.get(1), args.get(2));
            }
            if (fn.equals("math.lerp") && args.size() >= 3) {
                return new MolangExpressions.MathLerp(args.get(0), args.get(1), args.get(2));
            }
            if (fn.equals("math.floor") && args.size() >= 1) {
                return new MolangExpressions.MathFloor(args.get(0));
            }
            if (fn.equals("math.ceil") && args.size() >= 1) {
                return new MolangExpressions.MathCeil(args.get(0));
            }
            if (fn.equals("math.mod") && args.size() >= 2) {
                return new MolangExpressions.MathMod(args.get(0), args.get(1));
            }
            if (fn.equals("math.pow") && args.size() >= 2) {
                return new MolangExpressions.MathPow(args.get(0), args.get(1));
            }
            if (fn.equals("math.sqrt") && args.size() >= 1) {
                return new MolangExpressions.MathSqrt(args.get(0));
            }
            if (fn.equals("math.atan2") && args.size() >= 2) {
                return new MolangExpressions.MathAtan2(args.get(0), args.get(1));
            }
            if (fn.equals("math.min") && args.size() >= 2) {
                return new MolangExpressions.MathMin(args.get(0), args.get(1));
            }
            if (fn.equals("math.max") && args.size() >= 2) {
                return new MolangExpressions.MathMax(args.get(0), args.get(1));
            }
            if (fn.equals("math.die_roll") && args.size() >= 3) {
                return new MolangExpressions.MathDieRoll(args.get(0), args.get(1), args.get(2));
            }
            if (fn.equals("math.hermite_blend") && args.size() >= 1) {
                return new MolangExpressions.MathHermiteBlend(args.get(0));
            }

            // Unknown function fallback
            return IMolangExpression.ZERO;
        }

        private IMolangExpression parseNumber() {
            StringBuilder sb = new StringBuilder();
            if (peek() == '-') { // handle negative numbers if at start of number
                sb.append((char) next());
            }

            boolean hasDot = false;
            while (true) {
                int c = peek();
                if (Character.isDigit(c)) {
                    sb.append((char) next());
                } else if (c == '.' && !hasDot) {
                    sb.append((char) next());
                    hasDot = true;
                } else {
                    break;
                }
            }
            try {
                float val = Float.parseFloat(sb.toString());
                return IMolangExpression.constant(val);
            } catch (NumberFormatException e) {
                return IMolangExpression.ZERO;
            }
        }
    }
}
