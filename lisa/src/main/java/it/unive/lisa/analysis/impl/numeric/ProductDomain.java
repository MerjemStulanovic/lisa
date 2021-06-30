package it.unive.lisa.analysis.impl.numeric;

import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;

import java.util.Objects;

public class ProductDomain extends BaseNonRelationalValueDomain<ProductDomain> {

    private ExtSignDomain signDomain;
    private Parity parityDomain;

    private static final ProductDomain TOP = new ProductDomain(new ExtSignDomain(), new Parity());
    private static final ProductDomain BOTTOM = new ProductDomain(new ExtSignDomain().bottom(), new Parity().bottom());

    public ProductDomain() {
        this(new ExtSignDomain(), new Parity());
    }

    public ProductDomain(ExtSignDomain signDomain, Parity parityDomain) {
        this.signDomain = signDomain;
        this.parityDomain = parityDomain;
    }

    @Override
    protected ProductDomain evalNonNullConstant(Constant constant, ProgramPoint pp) {
        if (constant.getValue() instanceof Integer) {
            var value = (Integer) constant.getValue();

            Parity parity;

            if (value % 2 == 0) {
                parity = Parity.EVEN;
            } else {
                parity = Parity.ODD;
            }

            ExtSignDomain sign;

            if (value > 0) {
                sign = new ExtSignDomain(ExtSignDomain.Sign.PLUS);
            } else if (value < 0) {
                sign = new ExtSignDomain(ExtSignDomain.Sign.MINUS);
            } else {
                sign = new ExtSignDomain(ExtSignDomain.Sign.ZERO);
            }

            return new ProductDomain(sign, parity);
        }

        return top();
    }

    @Override
    protected ProductDomain evalNullConstant(ProgramPoint pp) {
        return top();
    }

    @Override
    protected ProductDomain evalUnaryExpression(UnaryOperator operator, ProductDomain arg, ProgramPoint pp) {
        if (operator == UnaryOperator.NUMERIC_NEG) {
            return ProductDomain.reduceProduct(new ProductDomain(new ExtSignDomain(arg.signDomain.sign.minus()), arg.parityDomain));
        }

        return top();
    }

    @Override
    protected ProductDomain evalBinaryExpression(BinaryOperator operator, ProductDomain left, ProductDomain right, ProgramPoint pp) {
        var sign = new ExtSignDomain().evalBinaryExpression(operator, left.signDomain, right.signDomain, pp);
        var parity = new Parity().evalBinaryExpression(operator, left.parityDomain, right.parityDomain, pp);

        return ProductDomain.reduceProduct(new ProductDomain(sign, parity));
    }

    protected static ProductDomain reduceProduct(ProductDomain productDomain) {
        var parityOddBool = productDomain.parityDomain == Parity.ODD;
        var extSignZeroMinusBool = productDomain.signDomain.sign == ExtSignDomain.Sign.ZERO_MINUS;
        var extSignZeroBool = productDomain.signDomain.sign == ExtSignDomain.Sign.ZERO;
        var extSignZeroPlusBool = productDomain.signDomain.sign == ExtSignDomain.Sign.ZERO_PLUS;

        var extSignMinus = new ExtSignDomain(ExtSignDomain.Sign.MINUS);
        var extSignPlus = new ExtSignDomain(ExtSignDomain.Sign.PLUS);
        var parityOdd = Parity.ODD;

        if (productDomain.signDomain.isBottom() || productDomain.parityDomain.isBottom()) {
            return productDomain.bottom();
        }

        if (extSignZeroMinusBool && parityOddBool) {
            return new ProductDomain(extSignMinus, parityOdd);
        }

        if (extSignZeroBool && parityOddBool) {
            return productDomain.bottom();
        }

        if (extSignZeroPlusBool && parityOddBool) {
            return new ProductDomain(extSignPlus, parityOdd);
        }

        return productDomain;
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesBinaryExpression(BinaryOperator operator, ProductDomain left, ProductDomain right, ProgramPoint pp) {
        if (operator == BinaryOperator.COMPARISON_EQ) {
            try {
                if (!left.lessOrEqual(right) && !right.lessOrEqual(left)) {
                    return SemanticDomain.Satisfiability.NOT_SATISFIED;
                }
            } catch (SemanticException ignored) {
            }
        }

        return SemanticDomain.Satisfiability.UNKNOWN;
    }

    @Override
    protected ProductDomain lubAux(ProductDomain other) throws SemanticException {
        var signLub = this.signDomain.lub(other.signDomain);
        var parityLub = this.parityDomain.lub(other.parityDomain);

        return ProductDomain.reduceProduct(new ProductDomain(signLub, parityLub));
    }

    @Override
    protected ProductDomain wideningAux(ProductDomain other) throws SemanticException {
        return lubAux(other);
    }

    @Override
    protected boolean lessOrEqualAux(ProductDomain other) throws SemanticException {
        if (other == null) {
            return false;
        }

        // An element is always <= than itself, bottom is <= than everything, everything is <= than top
        if (this == other || this.isBottom() || other.isTop() || this.equals(other)) {
            return true;
        }

        // Top is not <= anything, and nothing is <= bottom. (Except bottom and top which are covered above).
        if (this.isTop() || other.isBottom()) {
            return false;
        }

        // When there is an even/odd parity difference, two elements are incomparable.
        if (this.parityDomain.equals(Parity.EVEN) && other.parityDomain.equals(Parity.ODD)) {
            return false;
        }

        if (this.parityDomain.equals(Parity.ODD) && other.parityDomain.equals(Parity.EVEN)) {
            return false;
        }

        // If two elements have the same parity, compare them by sign.
        if (this.parityDomain.equals(other.parityDomain)) {
            return this.signDomain.lessOrEqualAux(other.signDomain);
        }

        // Conveniently, if the execution makes it to here, all the edges in the lattice are covered, except
        // for the ones where the nodes have the same sign, but different parities. Therefore, it suffices to
        // only compare by parity.
        if (this.parityDomain.equals(Parity.EVEN) || this.parityDomain.equals(Parity.ODD)) {
            return other.parityDomain.equals(Parity.TOP);
        }

        return false; // Unreachable.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductDomain)) {
            return false;
        }
        ProductDomain that = (ProductDomain) o;
        return Objects.equals(signDomain, that.signDomain) && Objects.equals(parityDomain, that.parityDomain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signDomain, parityDomain);
    }

    @Override
    public ProductDomain top() {
        return TOP;
    }

    @Override
    public ProductDomain bottom() {
        return BOTTOM;
    }

    @Override
    public boolean isTop() {
        return equals(TOP);
    }

    @Override
    public boolean isBottom() {
        return equals(BOTTOM);
    }

    @Override
    public ProductDomain variable(Identifier id, ProgramPoint pp) {
        return super.variable(id, pp);
    }

    @Override
    public DomainRepresentation representation() {
        return new StringRepresentation("(" + signDomain.representation() + ", " + parityDomain.representation() + ")");
    }
}
