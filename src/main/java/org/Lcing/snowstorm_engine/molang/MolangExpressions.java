package org.Lcing.snowstorm_engine.molang;

public class MolangExpressions {

    public static class Variable implements IMolangExpression {
        private final String name;

        public Variable(String name) {
            this.name = name;
        }

        @Override
        public float eval(MolangContext context) {
            return context.resolve(name);
        }
    }

    public static class UnaryMinus implements IMolangExpression {
        private final IMolangExpression inner;

        public UnaryMinus(IMolangExpression inner) {
            this.inner = inner;
        }

        @Override
        public float eval(MolangContext context) {
            return -inner.eval(context);
        }
    }

    public static class BinaryOp implements IMolangExpression {
        private final IMolangExpression left;
        private final IMolangExpression right;
        private final OpType type;

        public BinaryOp(IMolangExpression left, IMolangExpression right, OpType type) {
            this.left = left;
            this.right = right;
            this.type = type;
        }

        @Override
        public float eval(MolangContext context) {
            float l = left.eval(context);
            float r = right.eval(context);
            switch (type) {
                case ADD:
                    return l + r;
                case SUBTRACT:
                    return l - r;
                case MULTIPLY:
                    return l * r;
                case DIVIDE:
                    return r == 0 ? 0 : l / r;
                default:
                    return 0;
            }
        }

        public enum OpType {
            ADD, SUBTRACT, MULTIPLY, DIVIDE
        }
    }

    public static class MathRandom implements IMolangExpression {
        private final IMolangExpression min;
        private final IMolangExpression max;

        public MathRandom(IMolangExpression min, IMolangExpression max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public float eval(MolangContext context) {
            float minVal = min.eval(context);
            float maxVal = max.eval(context);
            return minVal + context.getRandom().nextFloat() * (maxVal - minVal);
        }
    }

    public static class MathSin implements IMolangExpression {
        private final IMolangExpression arg;

        public MathSin(IMolangExpression arg) {
            this.arg = arg;
        }

        @Override
        public float eval(MolangContext context) {
            // Molang uses degrees for trig!
            float deg = arg.eval(context);
            return (float) Math.sin(Math.toRadians(deg));
        }
    }

    public static class MathCos implements IMolangExpression {
        private final IMolangExpression arg;

        public MathCos(IMolangExpression arg) {
            this.arg = arg;
        }

        @Override
        public float eval(MolangContext context) {
            float deg = arg.eval(context);
            return (float) Math.cos(Math.toRadians(deg));
        }
    }

    public static class MathAbs implements IMolangExpression {
        private final IMolangExpression arg;

        public MathAbs(IMolangExpression arg) {
            this.arg = arg;
        }

        @Override
        public float eval(MolangContext ctx) {
            return Math.abs(arg.eval(ctx));
        }
    }

    public static class MathClamp implements IMolangExpression {
        private final IMolangExpression val, min, max;

        public MathClamp(IMolangExpression val, IMolangExpression min, IMolangExpression max) {
            this.val = val;
            this.min = min;
            this.max = max;
        }

        @Override
        public float eval(MolangContext ctx) {
            float v = val.eval(ctx);
            float mn = min.eval(ctx);
            float mx = max.eval(ctx);
            return Math.max(mn, Math.min(v, mx));
        }
    }

    public static class MathLerp implements IMolangExpression {
        private final IMolangExpression a, b, t;

        public MathLerp(IMolangExpression a, IMolangExpression b, IMolangExpression t) {
            this.a = a;
            this.b = b;
            this.t = t;
        }

        @Override
        public float eval(MolangContext ctx) {
            float av = a.eval(ctx);
            float bv = b.eval(ctx);
            float tv = t.eval(ctx);
            return av + (bv - av) * tv;
        }
    }

    public static class MathFloor implements IMolangExpression {
        private final IMolangExpression arg;

        public MathFloor(IMolangExpression arg) {
            this.arg = arg;
        }

        @Override
        public float eval(MolangContext ctx) {
            return (float) Math.floor(arg.eval(ctx));
        }
    }

    public static class MathCeil implements IMolangExpression {
        private final IMolangExpression arg;

        public MathCeil(IMolangExpression arg) {
            this.arg = arg;
        }

        @Override
        public float eval(MolangContext ctx) {
            return (float) Math.ceil(arg.eval(ctx));
        }
    }

    public static class MathMod implements IMolangExpression {
        private final IMolangExpression x, y;

        public MathMod(IMolangExpression x, IMolangExpression y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public float eval(MolangContext ctx) {
            float xv = x.eval(ctx);
            float yv = y.eval(ctx);
            if (yv == 0)
                return 0;
            return xv % yv;
        }
    }

    public static class MathPow implements IMolangExpression {
        private final IMolangExpression base, exp;

        public MathPow(IMolangExpression base, IMolangExpression exp) {
            this.base = base;
            this.exp = exp;
        }

        @Override
        public float eval(MolangContext ctx) {
            return (float) Math.pow(base.eval(ctx), exp.eval(ctx));
        }
    }

    public static class MathSqrt implements IMolangExpression {
        private final IMolangExpression arg;

        public MathSqrt(IMolangExpression arg) {
            this.arg = arg;
        }

        @Override
        public float eval(MolangContext ctx) {
            return (float) Math.sqrt(arg.eval(ctx));
        }
    }

    public static class MathAtan2 implements IMolangExpression {
        private final IMolangExpression y, x;

        public MathAtan2(IMolangExpression y, IMolangExpression x) {
            this.y = y;
            this.x = x;
        }

        @Override
        public float eval(MolangContext ctx) {
            return (float) Math.atan2(y.eval(ctx), x.eval(ctx));
        }
    }

    public static class MathMin implements IMolangExpression {
        private final IMolangExpression a, b;

        public MathMin(IMolangExpression a, IMolangExpression b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public float eval(MolangContext ctx) {
            return Math.min(a.eval(ctx), b.eval(ctx));
        }
    }

    public static class MathMax implements IMolangExpression {
        private final IMolangExpression a, b;

        public MathMax(IMolangExpression a, IMolangExpression b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public float eval(MolangContext ctx) {
            return Math.max(a.eval(ctx), b.eval(ctx));
        }
    }

    public static class MathDieRoll implements IMolangExpression {
        private final IMolangExpression num, low, high;

        public MathDieRoll(IMolangExpression num, IMolangExpression low, IMolangExpression high) {
            this.num = num;
            this.low = low;
            this.high = high;
        }

        @Override
        public float eval(MolangContext ctx) {
            int n = (int) num.eval(ctx);
            float lo = low.eval(ctx);
            float hi = high.eval(ctx);
            float sum = 0;
            for (int i = 0; i < n; i++) {
                sum += lo + ctx.getRandom().nextFloat() * (hi - lo);
            }
            return sum;
        }
    }

    public static class MathHermiteBlend implements IMolangExpression {
        private final IMolangExpression t;

        public MathHermiteBlend(IMolangExpression t) {
            this.t = t;
        }

        @Override
        public float eval(MolangContext ctx) {
            float tv = t.eval(ctx);
            // 3t^2 - 2t^3 (smooth step)
            return tv * tv * (3 - 2 * tv);
        }
    }
}
