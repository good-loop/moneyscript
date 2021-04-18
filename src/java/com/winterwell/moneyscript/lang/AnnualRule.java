package com.winterwell.moneyscript.lang;

import java.util.List;

import com.winterwell.moneyscript.lang.cells.CellSet;
import com.winterwell.moneyscript.lang.cells.RowName;
import com.winterwell.moneyscript.lang.num.Numerical;
import com.winterwell.moneyscript.output.Business;
import com.winterwell.moneyscript.output.Cell;
import com.winterwell.utils.TodoException;

public class AnnualRule extends MetaRule {

	public final String op;

	public AnnualRule(RowName selector, String op, String src) {
		super(selector, op, src);
		this.op = op;
	}	

	@Override
	protected Numerical calculate2_formula(Cell b) {
		throw new TodoException(this);
	}

	public Numerical calculateAnnual(Business b, List<Cell> cells, int i) {
		if ("off".equals(op)) {
			return null;
		}
		// sum the year
		if ("sum".equals(op)) {
			Numerical yearSum = new Numerical(0);
			for (int j=Math.max(0, i-11); j<=i; j++) {
				Cell cj = cells.get(j);
				Numerical vj = b.getCellValue(cj);
				if (vj==null) continue;
				yearSum = yearSum.plus(vj);				
			}
			yearSum.comment = "total for year"; //+t.getYear();
			return yearSum;
		}
		// previous
		if ("previous".equals(op)) {
			Cell cj = cells.get(i);
			Numerical vj = b.getCellValue(cj);
			Numerical yearEnd = new Numerical(vj);
			yearEnd.comment = "end of year";
		}
		// average
		if ("average".equals(op)) {
			Numerical yearSum = new Numerical(0);
			for (int j=Math.max(0, i-11); j<=i; j++) {
				Cell cj = cells.get(j);
				Numerical vj = b.getCellValue(cj);
				if (vj==null) continue;
				yearSum = yearSum.plus(vj);
			}
			yearSum = yearSum.divide(new Numerical(12));
			yearSum.comment = "average for year";
		}
		throw new TodoException(this);
	}
}