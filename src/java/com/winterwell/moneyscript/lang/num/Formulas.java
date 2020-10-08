package com.winterwell.moneyscript.lang.num;

import java.util.Collections;
import java.util.Set;

import com.winterwell.maths.stats.distributions.d1.UniformDistribution1D;
import com.winterwell.moneyscript.output.Business;
import com.winterwell.moneyscript.output.Cell;
import com.winterwell.moneyscript.output.Cell;
import com.winterwell.moneyscript.output.Col;
import com.winterwell.moneyscript.lang.UncertainNumerical;
import com.winterwell.moneyscript.lang.cells.CurrentRow;
import com.winterwell.moneyscript.lang.cells.Filter;
import com.winterwell.utils.TodoException;
import com.winterwell.utils.containers.ArraySet;
import com.winterwell.utils.containers.Range;
import com.winterwell.utils.log.Log;
import com.winterwell.utils.time.Dt;
import com.winterwell.utils.time.TUnit;

public class Formulas {
}



class Var extends Formula {

	private String var;

	public Var(String varName) {
		super("");
		this.var = varName;
	}

	@Override
	public Numerical calculate(Cell b) {
		if (var.equals("row")) {
			return new Numerical(b.getBusiness().getRowIndex(b.getRow()));
		}
		if (var.equals("column")) return new Numerical(b.col.index);
		if (var.equals("month")) {
			assert b.getBusiness().getTimeStep().equals(TUnit.MONTH.dt);
			return new Numerical(b.col.index);
		}		
		if (var.equals("year")) {
			assert b.getBusiness().getTimeStep().equals(TUnit.MONTH.dt);
			int yr = 1 + (int)Math.floor(b.getColumn().index/12);
			return new Numerical(yr);
		}
		// Note that previous(X) is handled by UnaryOp
		if (var.equals("previous")) {
			if (b.getColumn().index == 1) return new Numerical(0);
			Cell prevCell = new Cell(b.getRow(), new Col(b.getColumn().index-1));
			return b.getBusiness().getCellValue(prevCell);
		}
		throw new TodoException(var);
	}

}




class BinaryOp extends Formula {

	private Formula left;
	private Formula right;

	@Override
	public boolean isStacked() {
		return left.isStacked() || right.isStacked();
	}
	
	public BinaryOp(String op, Formula left, Formula right) {
		super(op);
//		assert ! (left instanceof SetValueFormula);
//		assert ! (right instanceof SetValueFormula);
		this.left = left;
		this.right = right;
		assert op != null;
		assert left != null && right != null;
	}
	
	@Override
	public Set<String> getRowNames() {
		ArraySet<String> set = new ArraySet<String>(left.getRowNames());
		set.addAll(right.getRowNames());
		return set;
	}

	@Override
	public Numerical calculate(Cell b) {
		Numerical x = left.calculate(b);
		if (x==null) x = Numerical.NULL;
		Numerical y = right.calculate(b);
		if (y==null) {
			if (x==Numerical.NULL) return null;
			y = Numerical.NULL;
		}
		
		// evaluating = 0? Sometimes. e.g. a payrise rule like `Staff: * 110%` vs a staff member starting later will
		// trigger an issue when applied to `Alice from month 3: £20k per year` -- as month 1 is 110% of evaluating.
		// This case is handled by treating as 0 (and not an error)
		if (x==Business.EVALUATING && left instanceof BasicFormula && ((BasicFormula) left).isCurrentRow()) {
			x = new Numerical(0);
		}
		if (y==Business.EVALUATING && right instanceof BasicFormula && ((BasicFormula) right).isCurrentRow()) {
			y = new Numerical(0);
		}
		
		if (x instanceof UncertainNumerical) Log.report("unexpected Uncertain: "+x+" from "+left);
		if (y instanceof UncertainNumerical) Log.report("unexpected Uncertain: "+y+" from "+right);
		
		if ("+"==op) {
			return x.plus(y);
		}
		if ("-"==op) {
			return x.plus( y.times(-1) );
		}
		if ("*"==op || "@"==op) {
			if (x==Numerical.NULL || y==Numerical.NULL) return null;
			return x.times(y);
		}
		if ("@"==op) { // like *, but preserves the LHS value for access
			if (x==Numerical.NULL || y==Numerical.NULL) return null;
			Numerical n = x.times(y);
			return new Numerical2(n, x);
		}
		if ("/"==op) {
			if (x==Numerical.NULL) return null;
			// what to do with divide by zero?
			return x.divide(y);
		}
		if ("+-".equals(op) || "±".equals(op)) {
			Range range = new Range(x.doubleValue()-y.doubleValue(), x.doubleValue()+y.doubleValue());
			UniformDistribution1D dist = new UniformDistribution1D(range);
			return sample(new UncertainNumerical(dist, DefaultCalculator.unit(x, y)));
		}
		if ("min".equals(op)) {
			double xy = Math.min(x.doubleValue(), y.doubleValue());
			return new Numerical(xy, DefaultCalculator.unit(x, y));
		}
		if ("max".equals(op)) {
			double xy = Math.max(x.doubleValue(), y.doubleValue());
			return new Numerical(xy, DefaultCalculator.unit(x, y));
		}
		if ("power".equals(op) || "^".equals(op)) {
			if (x==Numerical.NULL) return null;
			double xy = Math.pow(x.doubleValue(), y.doubleValue());
			return new Numerical(xy, x.getUnit()); // Keep the x unit
		}
		throw new TodoException(toString());
	}
	
	@Override
	public String toString() {
		return "("+left+op+right+")";
	}
}


/**
 * For 10 widgets @ £5 - which is £50, but the 10 is still accessible via #
 * @author daniel
 *
 */
class Numerical2 extends Numerical {
	private static final long serialVersionUID = 1L;
	private Numerical lhs;

	public Numerical2(Numerical n, Numerical lhs) {
		super(n.doubleValue(), n.getUnit());
		assert n.getClass() == Numerical.class : n; // no nesting, or unsampled uncertains
		this.lhs = lhs;
	}
	
	public Numerical getLhs() {
		return lhs;
	}
}