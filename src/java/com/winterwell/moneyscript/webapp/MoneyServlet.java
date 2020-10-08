package com.winterwell.moneyscript.webapp;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.winterwell.moneyscript.data.PlanDoc;
import com.winterwell.moneyscript.lang.Lang;
import com.winterwell.moneyscript.output.Business;
import com.winterwell.nlp.simpleparser.ParseExceptions;
import com.winterwell.nlp.simpleparser.ParseFail;
import com.winterwell.utils.containers.ArrayMap;
import com.winterwell.utils.containers.Containers;
import com.winterwell.web.ajax.JSend;
import com.winterwell.web.ajax.JThing;
import com.winterwell.web.ajax.KAjaxStatus;
import com.winterwell.web.app.IServlet;
import com.winterwell.web.app.WebRequest;

public class MoneyServlet implements IServlet {

	static Lang lang = new Lang();
	
	@Override
	public void process(WebRequest state) throws Exception {		
		String text = state.get("text");
		if (text==null) {
			return;
		}
		try {
			Business biz = lang.parse(text);
			
			// parse only?
			if (state.actionIs("parse")) {
				Map pi = biz.getParseInfoJson();
				JThing jt = new JThing().setJsonObject(pi);
				JSend jsend = new JSend(jt);
				jsend.send(state);				
			}
			
			// run!
			biz.run();
		
			ArrayMap json = biz.toJSON();
			JThing jt = new JThing().setJsonObject(json);
			JSend jsend = new JSend(jt);
			jsend.send(state);
		} catch(ParseFail pf) {
			processFail(Arrays.asList(pf), state);
		} catch(ParseExceptions pex) {
			processFail(pex.getErrors(), state);
		}
	}

	private void processFail(List<ParseFail> pfs, WebRequest state) {
		JSend jsend = new JSend();
		jsend.setStatus(KAjaxStatus.fail);
		List data = Containers.apply(pfs, pf -> pf.toJson2());
		jsend.setData(new ArrayMap("errors", data));
		jsend.send(state);
	}

}
