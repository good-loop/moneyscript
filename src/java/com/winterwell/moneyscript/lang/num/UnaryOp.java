package com.winterwell.moneyscript.lang.num;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.winterwell.moneyscript.lang.UncertainNumerical;
import com.winterwell.moneyscript.lang.cells.CellSet;
import com.winterwell.moneyscript.lang.time.DtDesc;
import com.winterwell.moneyscript.output.Business;
import com.winterwell.moneyscript.output.Cell;
import com.winterwell.moneyscript.output.Col;
import com.winterwell.moneyscript.output.Row;
import com.winterwell.utils.TodoException;
import com.winterwell.utils.Utils;
import com.winterwell.utils.containers.Containers;
import com.winterwell.utils.log.Log;

public class UnaryOp extends Formula {

	final Formula right;

	public UnaryOp(String op, Formula right) {
		super(op);
//		assert ! (right instanceof SetValueFormula);
		this.right = right;
	}
	
	@Override
	public boolean isStacked() {
		return right.isStacked();
	}
	
	@Override
	public Set<String> getRowNames() {
		return right.getRowNames();
	}

	@Override
	public Numerical calculate(Cell b) {
		if (op.startsWith("sum")) {
			return calculate2_sum(b);
		}
		if (op.startsWith("count")) {
			return calculate2_count(b);
		}
		if (op.equals("sqrt")) {
			Numerical x = right.calculate(b);
			if (x==null) return null;
			assert ! (x instanceof UncertainNumerical) : this;
			return new Numerical(Math.sqrt(x.doubleValue())); // any unit??
		}
		if (op.equals("log")) {
			Numerical x = right.calculate(b);
			if (x==null) return null;
			assert ! (x instanceof UncertainNumerical) : this;
			return new Numerical(Math.log(x.doubleValue())); // any unit??
		}
		// Probability
		if (op.equals("p")) {			
			Numerical x = right.calculate(b);			
			if (x==null) return null;
			double p = x.doubleValue();
			// hack x% per year = 1 - 12th rt (1 -x) per month
			// So we interpret p(10% per year) as P(at least once within a year) = 10%
			if (right instanceof PerFormula) {
				DtDesc dt = ((PerFormula)right).dt;				
				double n = dt.calculate(b).divide(b.getBusiness().getTimeStep());
				// undo the division
				p = n*p;
				p = 1 - Math.pow(1-p, 1/n);
			}						
			boolean yes = Utils.getRandomChoice(p);
			Numerical n = new Numerical(yes? 1 : 0);
			return n;
		}
		// ?? extract "3" from "3 @ £10" 
//		if (op=="#") {
//			Numerical x = right.calculate(b);
//			if (x==null) return Numerical.NULL;
//			assert x instanceof Numerical2;
//			return ((Numerical2)x).getLhs();
//		}
		// e.g. "previous Debt"
		if (op=="previous") {
			return calculate2_previous(b);
		}
		// Fail
		throw new TodoException(op+" "+right);
	}

	private Numerical calculate2_previous(Cell b) {
		// at the start? 0 then
		if (b.getColumn().index == 1) return new Numerical(0);			
		// must be a cell set as formula
		CellSet cellSet = ((BasicFormula)right).sel;
		assert cellSet != null : right;
		Set<String> rows = cellSet.getRowNames();
		assert rows.size() == 1 : rows;
		Row row = b.getBusiness().getRow(Containers.first(rows));
		assert row != null : rows;
		Cell prevCell = new Cell(row, new Col(b.getColumn().index-1));
//		Cell b2 = new Cell(prevCell);
		if ( ! cellSet.contains(prevCell, b)) {
			return null;
		}			
		return b.getBusiness().getCellValue(prevCell);
	}

	private Numerical calculate2_sum(Cell b) {
		Numerical sum = new Numerical(0);
		// eg "sum Sales"
		if (right instanceof BasicFormula) {
			// right should be a selector
			CellSet sel = ((BasicFormula)right).sel;
			// ?? FIXME sum Sales = sum (Sales from start to now)
//			sel.getStartColumn(sel.get, b);
			Collection<Cell> cells = sel.getCells(b, true);
			for (Cell cell : cells) {
				Numerical c = b.getBusiness().getCellValue(cell);
				sum = sum.plus(c);
			}
			return sum;
		}
		
		Collection<Cell> cells = b.getRow().getCells();
		// apply the op
		for (Cell cell : cells) {
			Numerical c = right.calculate(cell);
			sum = sum.plus(c);
		}		
		return sum;
	}


	/**
	 * count of non-zero values __in a column__
	 * Unpacks groups 
	 * @param b
	 * @return
	 */
	private Numerical calculate2_count(Cell b) {
		// eg "sum Sales"
		// right should be a selector
		CellSet sel = ((BasicFormula)right).sel;
		// get the rows
		List<String> rns = new ArrayList(sel.getRowNames());
		ArrayList<Row> leafRows = new ArrayList(); 
		Business biz = b.getBusiness();
		getLeafRows(Containers.apply(rns, biz::getRow), leafRows, biz);
		
		// apply the op
		int cnt = 0;
		for(Row row : leafRows) {
			Cell rcell = new Cell(row, b.getColumn());
			Numerical c = biz.getCellValue(rcell);
			if (c != null && c.doubleValue() != 0) {
				cnt++;
			}
		}
		return new Numerical(cnt);
	}

	
	private void getLeafRows(List<Row> agenda, ArrayList leafRows, Business b) {
		for(Row row : agenda) {
			if (row==null) {
				Log.w("UnaryOp "+this, "skipping null row in getLeafRows()");
				continue;
			}
			if (row.isGroup()) {
				List<Row> kids = row.getChildren();
				getLeafRows(kids, leafRows, b);
			} else {
				leafRows.add(row);
			}
		}		
	}
	

	@Override
	public String toString() {
		return op+" "+right;
	}
}