package it.unive.lisa.analysis.impl.numeric;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryOperator;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.TernaryOperator;
import it.unive.lisa.symbolic.value.UnaryOperator;

public final class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {
    enum Sign {

        ZERO {
            @Override
            Sign minus() {
                return ZERO;
            }

            @Override
            Sign add(Sign other) {
                /*
                 * add(0, bot) = bot
                 * add(0, 0) = 0
                 * add(0, +) = +
                 * add(0, -) = -
                 * add(0, 0+) = 0+
                 * add(0, 0-) = 0-
                 * add(0, top) = top
                 */

                return other;
            }

            @Override
            Sign div(Sign other) {
                /*
                 * div(0, bot) = bot
                 * div(0, 0) = bot
                 * div(0, +) = 0
                 * div(0, -) = 0
                 * div(0, 0+) = 0
                 * div(0, 0-) = 0
                 * div(0, top) = 0
                 */

                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }

                return ZERO;
            }

            @Override
            Sign mul(Sign other) {
                /*
                 * mul(0, bot) = bot
                 * mul(0, 0) = 0
                 * mul(0, +) = 0
                 * mul(0, -) = 0
                 * mul(0, 0+) = 0
                 * mul(0, 0-) = 0
                 * mul(0, top) = 0
                 */

                return other == BOTTOM ? BOTTOM : ZERO;
            }

            @Override
            Sign mod(Sign other) {
                /*
                 * mod(0, bot) = bot
                 * mod(0, 0) = bot
                 * mod(0, +) = 0
                 * mod(0, -) = 0
                 * mod(0, 0+) = 0
                 * mod(0, 0-) = 0
                 * mod(0, top) = 0
                 */

                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }

                return ZERO;
            }

            @Override
            public String toString() {
                return "0";
            }
        },

        PLUS {
            @Override
            Sign minus() {
                return MINUS;
            }

            @Override
            Sign add(Sign other) {
                /*
                 * add(+, bot) = bot
                 * add(+, 0) = +
                 * add(+, +) = +
                 * add(+, -) = top
                 * add(+, 0+) = +
                 * add(+, 0-) = top
                 * add(+, top) = top
                 */

                if (other == BOTTOM) {
                    return BOTTOM;
                }

                if (other == ZERO || other == PLUS || other == ZERO_PLUS) {
                    return PLUS;
                }

                return TOP;
            }

            @Override
            Sign div(Sign other) {
                /*
                 * div(+, bot) = bot
                 * div(+, 0) = bot
                 * div(+, +) = +
                 * div(+, -) = -
                 * div(+, 0+) = +
                 * div(+, 0-) = -
                 * div(+, top) = top
                 */

                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }

                if (other == PLUS || other == ZERO_PLUS) {
                    return PLUS;
                }

                if (other == MINUS || other == ZERO_MINUS) {
                    return MINUS;
                }

                return TOP;
            }

            @Override
            Sign mul(Sign other) {
                /*
                 * mul(+, bot) = bot
                 * mul(+, 0) = 0
                 * mul(+, +) = +
                 * mul(+, -) = -
                 * mul(+, 0+) = 0+
                 * mul(+, 0-) = 0-
                 * mul(+, top) = top
                 */
                return other;
            }

            @Override
            Sign mod(Sign other) {
                /*
                 * mod(+, bot) = bot
                 * mod(+, 0) = bot
                 * mod(+, +) = 0+
                 * mod(+, -) = 0+
                 * mod(+, 0+) = 0+
                 * mod(+, 0-) = 0+
                 * mod(+, top) = 0+
                 */

                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }

                return ZERO_PLUS;
            }

            @Override
            public String toString() {
                return "+";
            }
        },

        MINUS {
            @Override
            Sign minus() {
                return PLUS;
            }

            @Override
            Sign add(Sign other) {
                /*
                 * add(-, bot) = bot
                 * add(-, 0) = -
                 * add(-, +) = top
                 * add(-, -) = -
                 * add(-, 0+) = top
                 * add(-, 0-) = -
                 * add(-, top) = top
                 */

                if (other == BOTTOM) {
                    return BOTTOM;
                }

                if (other == ZERO || other == MINUS || other == ZERO_MINUS) {
                    return MINUS;
                }

                return TOP;
            }

            @Override
            Sign div(Sign other) {
                /*
                 * div(-, bot) = bot
                 * div(-, 0) = bot
                 * div(-, +) = -
                 * div(-, -) = +
                 * div(-, 0+) = -
                 * div(-, 0-) = +
                 * div(-, top) = top
                 */

                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }

                if (other == PLUS || other == ZERO_PLUS) {
                    return MINUS;
                }

                if (other == MINUS || other == ZERO_MINUS) {
                    return PLUS;
                }

                return TOP;
            }

            @Override
            Sign mul(Sign other) {
                /*
                 * mul(-, bot) = bot
                 * mul(-, 0) = 0
                 * mul(-, +) = -
                 * mul(-, -) = +
                 * mul(-, 0+) = 0-
                 * mul(-, 0-) = 0+
                 * mul(-, top) = top
                 */

                return other.minus();
            }

            @Override
            Sign mod(Sign other) {
                /*
                 * mod(-, bot) = bot
                 * mod(-, 0) = bot
                 * mod(-, +) = 0-
                 * mod(-, -) = 0-
                 * mod(-, 0+) = 0-
                 * mod(-, 0-) = 0-
                 * mod(-, top) = 0-
                 */

                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }

                return ZERO_MINUS;
            }

            @Override
            public String toString() {
                return "-";
            }
        },

        ZERO_PLUS {
            @Override
            Sign minus() {
                return ZERO_MINUS;
            }

            @Override
            Sign add(Sign other) {
                /*
                 * add(0+, bot) = bot
                 * add(0+, 0) = 0+
                 * add(0+, +) = +
                 * add(0+, -) = top
                 * add(0+, 0+) = 0+
                 * add(0+, 0-) = top
                 * add(0+, top) = top
                 */
                if (other == BOTTOM) {
                    return BOTTOM;
                }

                if (other == ZERO || other == ZERO_PLUS) {
                    return ZERO_PLUS;
                }

                if (other == PLUS) {
                    return PLUS;
                }

                return TOP;
            }

            @Override
            Sign div(Sign other) {
                /*
                 * div(0+, bot) = bot
                 * div(0+, 0) = bot
                 * div(0+, +) = 0+
                 * div(0+, -) = 0-
                 * div(0+, 0+) = 0+
                 * div(0+, 0-) = 0-
                 * div(0+, top) = top
                 */

                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }

                if (other == PLUS || other == ZERO_PLUS) {
                    return ZERO_PLUS;
                }

                if (other == MINUS || other == ZERO_MINUS) {
                    return ZERO_MINUS;
                }

                return TOP;
            }

            @Override
            Sign mul(Sign other) {
                /*
                 * mul(0+, bot) = bot
                 * mul(0+, 0) = 0
                 * mul(0+, +) = 0+
                 * mul(0+, -) = 0-
                 * mul(0+, 0+) = 0+
                 * mul(0+, 0-) = 0-
                 * mul(0+, top) = top
                 */

                if (other == BOTTOM) {
                    return BOTTOM;
                }

                if (other == ZERO) {
                    return ZERO;
                }

                if (other == PLUS || other == ZERO_PLUS) {
                    return ZERO_PLUS;
                }

                if (other == MINUS || other == ZERO_MINUS) {
                    return ZERO_MINUS;
                }

                return TOP;
            }

            @Override
            Sign mod(Sign other) {
                /*
                 * mod(0+, bot) = bot
                 * mod(0+, 0) = bot
                 * mod(0+, +) = 0+
                 * mod(0+, -) = 0+
                 * mod(0+, 0+) = 0+
                 * mod(0+, 0-) = 0+
                 * mod(0+, top) = 0+
                 */

                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }

                return ZERO_PLUS;
            }

            @Override
            public String toString() {
                return "0+";
            }
        },

        ZERO_MINUS {
            @Override
            Sign minus() {
                return ZERO_PLUS;
            }

            @Override
            Sign add(Sign other) {
                /*
                 * add(0-, bot) = bot
                 * add(0-, 0) = 0-
                 * add(0-, +) = top
                 * add(0-, -) = -
                 * add(0-, 0+) = top
                 * add(0-, 0-) = 0-
                 * add(0-, top) = top
                 */

                if (other == BOTTOM) {
                    return BOTTOM;
                }

                if (other == ZERO || other == ZERO_MINUS) {
                    return ZERO_MINUS;
                }

                if (other == MINUS) {
                    return MINUS;
                }

                return TOP;
            }

            @Override
            Sign div(Sign other) {
                /*
                 * div(0-, bot) = bot
                 * div(0-, 0) = bot
                 * div(0-, +) = 0-
                 * div(0-, -) = 0+
                 * div(0-, 0+) = 0-
                 * div(0-, 0-) = 0+
                 * div(0-, top) = top
                 */

                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }

                if (other == PLUS || other == ZERO_PLUS) {
                    return ZERO_MINUS;
                }

                if (other == MINUS || other == ZERO_MINUS) {
                    return ZERO_PLUS;
                }

                return TOP;
            }

            @Override
            Sign mul(Sign other) {
                /*
                 * mul(0-, bot) = bot
                 * mul(0-, 0) = 0
                 * mul(0-, +) = 0-
                 * mul(0-, -) = 0+
                 * mul(0-, 0+) = 0-
                 * mul(0-, 0-) = 0+
                 * mul(0-, top) = top
                 */

                if (other == BOTTOM) {
                    return BOTTOM;
                }

                if (other == ZERO) {
                    return ZERO;
                }

                if (other == PLUS || other == ZERO_PLUS) {
                    return ZERO_MINUS;
                }

                if (other == MINUS || other == ZERO_MINUS) {
                    return ZERO_PLUS;
                }

                return TOP;
            }

            @Override
            Sign mod(Sign other) {
                /*
                 * mod(0-, bot) = bot
                 * mod(0-, 0) = bot
                 * mod(0-, +) = 0-
                 * mod(0-, -) = 0-
                 * mod(0-, 0+) = 0-
                 * mod(0-, 0-) = 0-
                 * mod(0-, top) = 0-
                 */

                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }

                return ZERO_MINUS;
            }

            @Override
            public String toString() {
                return "0-";
            }
        },

        TOP {
            @Override
            Sign minus() {
                return TOP;
            }

            @Override
            Sign add(Sign other) {
                /*
                 * add(top, bot) = bot
                 * add(top, 0) = top
                 * add(top, +) = top
                 * add(top, -) = top
                 * add(top, 0+) = top
                 * add(top, 0-) = top
                 * add(top, top) = top
                 */

                return other == BOTTOM ? BOTTOM : TOP;
            }

            @Override
            Sign div(Sign other) {
                /*
                 * div(top, bot) = bot
                 * div(top, 0) = bot
                 * div(top, +) = top
                 * div(top, -) = top
                 * div(top, 0+) = top
                 * div(top, 0-) = top
                 * div(top, top) = top
                 */

                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }

                return TOP;
            }

            @Override
            Sign mul(Sign other) {
                /*
                 * mul(top, bot) = bot
                 * mul(top, 0) = 0
                 * mul(top, +) = top
                 * mul(top, -) = top
                 * mul(top, 0+) = top
                 * mul(top, 0-) = top
                 * mul(top, top) = top
                 */

                if (other == BOTTOM) {
                    return BOTTOM;
                }

                if (other == ZERO) {
                    return ZERO;
                }

                return TOP;
            }

            @Override
            Sign mod(Sign other) {
                /*
                 * mod(top, bot) = bot
                 * mod(top, 0) = bot
                 * mod(top, +) = top
                 * mod(top, -) = top
                 * mod(top, 0+) = top
                 * mod(top, 0-) = top
                 * mod(top, top) = top
                 */

                if (other == BOTTOM || other == ZERO) {
                    return BOTTOM;
                }

                return TOP;
            }

            @Override
            public String toString() {
                return Lattice.TOP_STRING;
            }
        },

        BOTTOM {
            @Override
            Sign minus() {
                return BOTTOM;
            }

            @Override
            Sign add(Sign other) {
                /*
                 * add(bot, bot) = bot
                 * add(bot, 0) = bot
                 * add(bot, +) = bot
                 * add(bot, -) = bot
                 * add(bot, 0+) = bot
                 * add(bot, 0-) = bot
                 * add(bot, top) = bot
                 */

                return BOTTOM;
            }

            @Override
            Sign div(Sign other) {
                /*
                 * div(bot, bot) = bot
                 * div(bot, 0) = bot
                 * div(bot, +) = bot
                 * div(bot, -) = bot
                 * div(bot, 0+) = bot
                 * div(bot, 0-) = bot
                 * div(bot, top) = bot
                 */

                return BOTTOM;
            }

            @Override
            Sign mul(Sign other) {
                /*
                 * mul(bot, bot) = bot
                 * mul(bot, 0) = bot
                 * mul(bot, +) = bot
                 * mul(bot, -) = bot
                 * mul(bot, 0+) = bot
                 * mul(bot, 0-) = bot
                 * mul(bot, top) = bot
                 */

                return BOTTOM;
            }

            @Override
            Sign mod(Sign other) {
                /*
                 * mod(bot, bot) = bot
                 * mod(bot, 0) = bot
                 * mod(bot, +) = bot
                 * mod(bot, -) = bot
                 * mod(bot, 0+) = bot
                 * mod(bot, 0-) = bot
                 * mod(bot, top) = bot
                 */

                return BOTTOM;
            }

            @Override
            public String toString() {
                return Lattice.BOTTOM_STRING;
            }
        };


        abstract Sign minus();

        abstract Sign add(Sign other);

        final Sign sub(Sign other) {
            return add(other.minus());
        }

        abstract Sign div(Sign other);

        abstract Sign mul(Sign other);

        abstract Sign mod(Sign other);

        @Override
        public abstract String toString();
    }

    public final Sign sign;

    public ExtSignDomain() {
        this(Sign.TOP);
    }

    public ExtSignDomain(Sign sign) {
        this.sign = sign;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sign == null) ? 0 : sign.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExtSignDomain other = (ExtSignDomain) obj;
        if (sign.ordinal() != other.sign.ordinal()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isTop() {
        return equals(top());
    }

    @Override
    public boolean isBottom() {
        return equals(bottom());
    }

    @Override
    public ExtSignDomain top() {
        return new ExtSignDomain(Sign.TOP);
    }

    @Override
    public ExtSignDomain bottom() {
        return new ExtSignDomain(Sign.BOTTOM);
    }

    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation(sign.toString());
    }

    @Override
    protected ExtSignDomain evalNullConstant(ProgramPoint pp) {
        return top();
    }

    @Override
    protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) {
        if (constant.getValue() instanceof Integer) {
            int c = (int) constant.getValue();
            if (c == 0) {
                return new ExtSignDomain(Sign.ZERO);
            } else if (c > 0) {
                return new ExtSignDomain(Sign.PLUS);
            } else {
                return new ExtSignDomain(Sign.MINUS);
            }
        }
        return top();
    }

    @Override
    protected ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) {
        switch (operator) {
            case NUMERIC_NEG:
                return new ExtSignDomain(arg.sign.minus());
            default:
                return top();
        }
    }

    @Override
    protected ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) {
        switch (operator) {
            case NUMERIC_ADD:
                return new ExtSignDomain(left.sign.add(right.sign));
            case NUMERIC_DIV:
                return new ExtSignDomain(left.sign.div(right.sign));
            case NUMERIC_MOD:
                return new ExtSignDomain(left.sign.mod(right.sign));
            case NUMERIC_MUL:
                return new ExtSignDomain(left.sign.mul(right.sign));
            case NUMERIC_SUB:
                return new ExtSignDomain(left.sign.sub(right.sign));
            default:
                return top();

        }
    }

    @Override
    protected ExtSignDomain evalTernaryExpression(TernaryOperator operator, ExtSignDomain left, ExtSignDomain middle, ExtSignDomain right, ProgramPoint pp) {
        return top();
    }

    @Override
    protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if (sign == other.sign) {
            return new ExtSignDomain(sign);
        }

        if (sign == Sign.BOTTOM) {
            return new ExtSignDomain(other.sign);
        }

        if (other.sign == Sign.BOTTOM) {
            return new ExtSignDomain(sign);
        }

        var thisZP = sign == Sign.ZERO || sign == Sign.PLUS || sign == Sign.ZERO_PLUS;
        var otherZP = other.sign == Sign.ZERO || other.sign == Sign.PLUS || other.sign == Sign.ZERO_PLUS;

        if (thisZP && otherZP) {
            return new ExtSignDomain(Sign.ZERO_PLUS);
        }

        var thisZM = sign == Sign.ZERO || sign == Sign.MINUS || sign == Sign.ZERO_MINUS;
        var otherZM = other.sign == Sign.ZERO || other.sign == Sign.MINUS || other.sign == Sign.ZERO_MINUS;

        if (thisZM && otherZM) {
            return new ExtSignDomain(Sign.ZERO_MINUS);
        }

        return top();
    }

    @Override
    protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        return lubAux(other);
    }

    @Override
    protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        if (other.sign == Sign.ZERO_MINUS) {
            return sign == Sign.MINUS || sign == Sign.ZERO;
        }

        if (other.sign == Sign.ZERO_PLUS) {
            return sign == Sign.PLUS || sign == Sign.ZERO;
        }

        return false;
    }
}
