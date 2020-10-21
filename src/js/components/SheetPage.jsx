import React, { Component } from 'react';
import {ReactDOM} from 'react-dom';
import {SJTest} from 'sjtest';
import {Login} from 'you-again';
import printer from '../base/utils/printer';
import C from '../C';
import Roles from '../base/Roles';
import Misc from '../base/components/Misc';
import {stopEvent, modifyHash} from '../base/utils/miscutils';
import DataStore from '../base/plumbing/DataStore';
import Settings from '../base/Settings';
import ShareWidget, {ShareLink} from '../base/components/ShareWidget';
import ListLoad, {CreateButton, ListItems} from '../base/components/ListLoad';
import ActionMan from '../plumbing/ActionMan';
import PropControl from '../base/components/PropControl';
import JSend from '../base/data/JSend';
import SimpleTable from '../base/components/SimpleTable';
import {setTaskTags} from '../base/components/TaskList';
import ServerIO from '../plumbing/ServerIO';
import ViewCharts from './ViewCharts';
import ViewSpreadSheet from './ViewSpreadsheet';
import _ from 'lodash';
import { getPlanId } from './MoneyScriptEditorPage';
import { Alert, Col, Row } from 'reactstrap';
import ErrorAlert from '../base/components/ErrorAlert';

// import brace from 'brace';
// import AceEditor from 'react-ace';
// import 'brace/mode/java';
// import 'brace/theme/github';


const SheetPage = () => {
	// which plan?
	const id = getPlanId();
	if ( ! id) {
		return <Alert color='warning'>No plan ID - go to <a href='#Plan'>Plans</a> to select or create one</Alert>;
	}
	// load
	const type = C.TYPES.PlanDoc;
	const pvItem = ActionMan.getDataItem({type, id, status:C.KStatus.DRAFT});
	if ( ! pvItem.resolved) {
		return (<div><h1>{type}: {id}</h1><Misc.Loading /></div>);
	}
	if (pvItem.error) {
		return (<div><h1>{type}: {id}</h1><ErrorAlert error={pvItem.error} /></div>);
	}
	const item = pvItem.value;
	window.document.title = "M$: "+item.name;

	return <>
		<Row>
			<Col md={6}><a href={'/#plan/'+escape(id)}>&lt; View Plan</a></Col>
			<Col md={6}><h2>{item.name || item.id}</h2></Col>
		</Row>
		<ViewSpreadSheet plandoc={item} />
	</>;
};

SheetPage.fullWidth = true;
export default SheetPage;
