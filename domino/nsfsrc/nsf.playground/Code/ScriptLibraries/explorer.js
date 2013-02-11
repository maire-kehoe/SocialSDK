/**
 * Update the label in the toolbat 
 */
function updateLabel(id) {
	var tt = dojo.byId("CurrentLabel");
	if(tt) {
		tt.innerHTML = id; 
	}
}

function emptyAPI() {
	updateLabel("");
	//updateNavSelection();
}

function loadAPI(id) {
	XSP.showContent(pageGlobal.dynPanel,"api",{api:id});
	updateLabel(id);
	//updateNavSelection();
}

function toggleSection(id) {
	var d = dojo.byId(id);
	if(d) {
		if(d.style.display=="none") {
			dojo.fx.wipeIn({node:d, duration:100}).play();
		} else {
			dojo.fx.wipeOut({node:d, duration:100}).play();
		}
	}
}

function expandSection(id) {
	var d = dojo.byId(id);
	if(d) {
		if(d.style.display=="none") {
			dojo.fx.wipeIn({node:d, duration:100}).play();
		}
	}
}

function collapseSection(id) {
	var d = dojo.byId(id);
	if(d) {
		if(d.style.display!="none") {
			dojo.fx.wipeOut({node:d, duration:100}).play();
		}
	}
}

function executeService(params,details,results) {
	require(['dojo/_base/lang','dojo/io-query','dojo/query','sbt/Endpoint'], function(lang,ioQuery,query,Endpoint) {
		function paramValue(name) {
			var n = query("[data-param=\""+name+"\"]",details);
			if(n.length>0) {
				return n[0].value;
			}
			return null;
		}
		var item = params.item;
		if(!params['endpoint']) {
			updatePanelError(results,"No endpoint specified in the API description");
			return;
		}
		var ep = Endpoint.find(params['endpoint']);
		if(!ep) {
			updatePanelError(results,"Endpoint {0} specified in the API description is invalid",params['endpoint']);
			return;
		}
		var m = item.http_method;
		if(!m) {
			updatePanelError(results,"No HTTP method specified in the API description");
			return;
		}
		
		// Transform the URI with the macros
		var uri = item.uri;
		if(item.uriParameters) {
			var subst = {}
			var pp = item.uriParameters;
			for(var i=0; i<pp.length; i++) {
				var r = paramValue(pp[i].name);
				if(r) {
					subst[pp[i].name] = r;
				}
			}
			uri = lang.replace(uri,subst);
			//alert("URI="+uri)
		}
		
		// Compose the query string
		if(item.queryParameters) {
			var qp = {}
			var pp = item.queryParameters;
			for(var i=0; i<pp.length; i++) {
				var r = paramValue(pp[i].name);
				if(r) {
					qp[pp[i].name] = r;
				}
			}
			var s = ioQuery.objectToQuery(qp)
			if(s) {
				uri += (uri.match(/\?/) ? '&' : '?') + s; 
			}
		}
		var qs = paramValue("query-string");
		if(qs) {
			uri += (uri.match(/\?/) ? '&' : '?') + encodeURI(qs); 
		}
		
		var args = {
			serviceUrl : uri,
			handleAs : "text",
	    	load : function(response,ioArgs) {
	    		updatePanel(results,200,"",response,ioArgs);
	    	},
	    	error : function(error,ioArgs) {
	    		updatePanel(results,"","",error,ioArgs);
	    	}
		};
		var body = null;
		ep.xhr(m,args,body);
	});
}

function updatePanel(id,code,headers,body,ioArgs) {
	updateResponse(id,code,headers,prettify(body.trim(),ioArgs));
}

function updatePanelError(id,error,ioArgs) {
	updateResponse(id,ioArgs.xhr.status,"","Error\n"+error);
}

function clearResultsPanel(id,code,headers,body) {
	updateResponse(id,"","","");
}

function updateResponse(id,code,headers,body,ioArgs) {
	require(['dojo/query','sbt/dom'], function(query,dom) {
		dom.setText(query(".respCode",id)[0],code);
		dom.setText(query(".respHeader",id)[0],headers);
		dom.setText(query(".respBody",id)[0],body);
	});
}

function prettify(s,ioArgs) {
	try {
		// Check for XML result
		if(s.indexOf("<?xml")==0) {
			return prettifyXml(s);
		}
		// Check for Json result
		if(s.indexOf("{")==0) {
			return prettifyJson(s);
		}
	} catch(e) {} // Return the initial string
	return s;
} 

// TEMP
function prettifyXml(xml) {
	var reg = /(>)(<)(\/*)/g;
    var wsexp = / *(.*) +\n/g;
    var contexp = /(<.+>)(.+\n)/g;
    xml = xml.replace(reg, '$1\n$2$3').replace(wsexp, '$1\n').replace(contexp, '$1\n$2');
    var pad = 0;
    var formatted = '';
    var lines = xml.split('\n');
    var indent = 0;
    var lastType = 'other';
    // 4 types of tags - single, closing, opening, other (text, doctype, comment) - 4*4 = 16 transitions 
    var transitions = {
        'single->single'    : 0,
        'single->closing'   : -1,
        'single->opening'   : 0,
        'single->other'     : 0,
        'closing->single'   : 0,
        'closing->closing'  : -1,
        'closing->opening'  : 0,
        'closing->other'    : 0,
        'opening->single'   : 1,
        'opening->closing'  : 0, 
        'opening->opening'  : 1,
        'opening->other'    : 1,
        'other->single'     : 0,
        'other->closing'    : -1,
        'other->opening'    : 0,
        'other->other'      : 0
    };

    for (var i=0; i < lines.length; i++) {
        var ln = lines[i];
        var single = Boolean(ln.match(/<.+\/>/)); // is this line a single tag? ex. <br />
        var closing = Boolean(ln.match(/<\/.+>/)); // is this a closing tag? ex. </a>
        var opening = Boolean(ln.match(/<[^!].*>/)); // is this even a tag (that's not <!something>)
        var type = single ? 'single' : closing ? 'closing' : opening ? 'opening' : 'other';
        var fromTo = lastType + '->' + type;
        lastType = type;
        var padding = '';

        indent += transitions[fromTo];
        for (var j = 0; j < indent; j++) {
            padding += '    ';
        }

        formatted += padding + ln + '\n';
    }

    return formatted;
}

function prettifyJson(json) {
	return json;
}
