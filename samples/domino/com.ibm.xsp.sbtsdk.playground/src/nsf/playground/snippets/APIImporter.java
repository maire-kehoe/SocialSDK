package nsf.playground.snippets;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import lotus.domino.Database;
import lotus.domino.Document;
import nsf.playground.beans.APIBean;
import nsf.playground.extension.ImportOptions;
import nsf.playground.extension.PlaygroundExtensionFactory;
import nsf.playground.jobs.AsyncAction;
import nsf.playground.json.JsonJavaObjectI;

import com.ibm.commons.runtime.util.URLEncoding;
import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonGenerator;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonParser;
import com.ibm.sbt.playground.assets.Asset;
import com.ibm.sbt.playground.assets.AssetNode;
import com.ibm.sbt.playground.assets.Node;
import com.ibm.sbt.playground.assets.NodeFactory;
import com.ibm.sbt.playground.assets.apis.APIDescription;
import com.ibm.sbt.playground.assets.apis.APINodeFactory;
import com.ibm.sbt.playground.vfs.VFSFile;
import com.ibm.sbt.services.client.ClientServicesException;
import com.ibm.sbt.services.client.RestClient;

/**
 * Class for importing API Descriptions.
 * 
 * @author priand
 *
 */
public class APIImporter extends AssetImporter {

	public static final String TYPE = "api";
	public static final String FORM = APIBean.FORM;
	
	public APIImporter(Database db) {
		super(db);
	}
	
	protected String getAssetType() {
		return TYPE;
	}

	protected String getAssetForm() {
		return FORM;
	}

	protected NodeFactory getNodeFactory() {
		return new APINodeFactory();
	}

	@Override
	protected void saveAsset(ImportSource source, VFSFile root, AssetNode node, Asset asset) throws Exception {
		APIDescription snippet = (APIDescription)asset;
		String[] products = StringUtil.splitString(snippet.getProperty("runtimes"),',');
		saveAsset(node.getUnid(), node.getCategory(), node.getName(), source.getName(), snippet.getJson(), products, snippet.getPropertiesAsString());
	}

	protected void saveAsset(String id, String category, String name, String source, String json, String[] products, String properties) throws Exception {
		Document doc = getDatabase().createDocument();
		try {
			setItemValue(doc,"Form", FORM);
			setItemValue(doc,"Author", doc.getParentDatabase().getParent().getEffectiveUserName()); // Should we make this private (reader field)?
			setItemValue(doc,"Id", id);
			setItemValue(doc,"Category", category);
			setItemValue(doc,"Name", name);
			setItemValue(doc,"ImportSource", source);
			setItemValueRichText(doc,"Json", json);
			setItemValueRichText(doc,"Properties", properties);
			
			if(products!=null && products.length>0) {
				setItemValue(doc,"FilterRuntimes", StringUtil.concatStrings(products, ',', true));
			}
			
			doc.save();
		} finally {
			doc.recycle();
		}
	}
	
	@Override
	protected int importAssets(ImportSource source, final AsyncAction action) throws Exception {
		if(StringUtil.equals(source.getSource(), "apidoc")) {
			String location=source.getLocation().trim();
			return importAssetsNSF(source, location, action);
		} else {
			return super.importAssets(source, action);
		}
	}

	protected int importAssetsNSF(ImportSource source, String location, AsyncAction action) throws Exception {
		Map<String,APIDocument> apiDocs = new HashMap<String, APIImporter.APIDocument>();

		if(StringUtil.isEmpty(location)) {
			location = PathUtil.concat("http://127.0.0.1",getDatabase().getFilePath(),'/');
		}
		
		RestClient client = createDominoClient(location,source.getUserName(),source.getPassword());
		
		List<DocEntry> list = loadEntries(client,source);
		if(action!=null&&action.isCancelled()) {
			return 0;
		}
		for(int i=0; i<list.size(); i++) {
			if(action!=null&&action.isCancelled()) {
				return 0;
			}
			importAssetsNSF(client, source, apiDocs, list.get(i),action);
		}

		// Get the import options for all the platforms
		List<ImportOptions> options = PlaygroundExtensionFactory.getExtensions(ImportOptions.class);
		
		for(Map.Entry<String, APIDocument> ed: apiDocs.entrySet()) {
			String[] products = ed.getValue().products;
			String path = adjustPath(options,products,ed.getValue().path);
			String id = Node.encodeUnid(path);
			String category = trimSeparator(extractCategory(path));
			String name = trimSeparator(extractName(path));
			String json = JsonGenerator.toJson(JsonJavaFactory.instanceEx, ed.getValue().content);
			String properties = createPropertiesAsString(options,products,path);

			saveAsset(id, category, name, source.getName(), json, products, properties);
		}
		
		return apiDocs.size();
	}

	private String adjustPath(List<ImportOptions> options, String[] products, String path) throws IOException {
		for(int i=0; i<options.size(); i++) {
			String adjustedPath = options.get(i).adjustExplorerPath(products, path);
			if(StringUtil.isNotEmpty(adjustedPath)) {
				return adjustedPath;
			}
		}
		return path;
	}
	private String createPropertiesAsString(List<ImportOptions> options, String[] products, String path) throws IOException {
		Properties properties = new Properties();
		
		for(int i=0; i<options.size(); i++) {
			options.get(i).createProperties(properties, products, path);
		}
		StringWriter sw = new StringWriter();
		properties.store(sw, null);
		return sw.toString();
	}
	private String extractCategory(String path) {
		int pos = path.lastIndexOf('/');
		if(pos>=0) {
			return path.substring(0,pos);
		}
		return "";
	}
	private String extractName(String path) {
		int pos = path.lastIndexOf('/');
		if(pos>=0) {
			return path.substring(pos+1);
		}
		return path;
	}
	private String trimSeparator(String path) {
		if(path!=null) {
			if(path.startsWith("/")) {
				path = path.substring(1);
			}
			if(path.endsWith("/")) {
				path = path.substring(0,path.length()-1);
			}
		}
		return path;
	}

	protected void importAssetsNSF(RestClient client, ImportSource source, Map<String,APIDocument> apiDocs, DocEntry entry, AsyncAction action) throws Exception {
		JsonJavaObject doc = loadAPIDocument(client, entry);
		if(doc!=null) {
			//JsonDump.dumpObject(JsonJavaFactory.instanceEx2, doc);
			if(action!=null) {
				String title = (String)doc.get("vtitle");
				if(StringUtil.isEmpty(title)) {
					title = "Documentation...";
				}
				action.updateTask("Importing: {0}", title);
			}
			String apiExplorerPath = trimSeparator(doc.getString("APIExplorerPath"));
			int pos = apiExplorerPath.indexOf('#');
			if(pos>=0) {
				apiExplorerPath = apiExplorerPath.substring(0,pos); 
			}
			
			String[] products = StringUtil.splitString(doc.getString("Products"),',');
			fixProducts(products);
			if(!containsRuntime(source, products)) {
				return;
			}
			
			String s = doc.toString();
			List mt = (List)doc.get("RequestsDetails");
			if(mt!=null) {
				for(int i=0; i<mt.size(); i++) {
					APIDocument apiDoc = apiDocs.get(apiExplorerPath);
					if(apiDoc==null) {
						apiDoc = new APIDocument(products,apiExplorerPath);
						apiDocs.put(apiExplorerPath, apiDoc);
					}
					JsonJavaObject je = createAPIEntry(client, doc, i, entry.unid, action);
					apiDoc.content.add(je);
				}
			}
		}
	}
	protected void fixProducts(String[] products) {
		// Fix the product labels coming from the Wikis
		if(products!=null) {
			for(int i=0; i<products.length; i++) {
				String p = products[i];
				if(StringUtil.indexOfIgnoreCase(p, "domino")>=0) {
					products[i] = "domino";
				} else if(StringUtil.indexOfIgnoreCase(p, "connections")>=0) {
					products[i] = "connections";
				} else if(StringUtil.indexOfIgnoreCase(p, "smartcloud")>=0) {
					products[i] = "smartcloud";
				}
			}
		}
	}
	protected boolean containsRuntime(ImportSource source, String[] products) {
		String[] filters = source.getRuntimes();
		if(filters==null || filters.length==0) {
			// No filters...
			return true;
		}
		for(int i=0; i<filters.length; i++) {
			String filter = filters[i];
			for(int j=0; j<products.length; j++) {
				String product = products[j];
				if(StringUtil.equals(filter,product)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected JsonJavaObject createAPIEntry(RestClient client, JsonJavaObject e, int method, String notesId, AsyncAction action) throws Exception {
		JsonJavaObject je = new JsonJavaObject();
		
		// IBM Doc Wiki
		String name = (String)e.get("displaysubject");
		if(StringUtil.isEmpty(name)) {
			// Regular Playground Wiki
			name = (String)e.get("Title");
		}
		put(je,"name", name);
		put(je,"description", e.get("Abstract"));
		
		// UNID for the documentation entry
		String unid=Node.encodeUnid(name);
		if(method>1) {
			unid = unid + "_" + Integer.toString(method);
		}
		put(je,"unid", unid);
		
		JsonJavaObject rd = (JsonJavaObject)((List)e.get("RequestsDetails")).get(method);
		if(rd!=null) {
			put(je,"http_method", rd.get("method") );
			put(je,"post_content_type", e.get("RequestContentType") );
			put(je,"post_content", e.get("RequestSample") );
			String uri = rd.getString("uri");
			put(je,"uri",uri);
			// Make sure that the URI parameters are all in the uri 
			// Remove the one that are not in the URI
			if(e.get("URLParameters")!=null) {
				List p = (List)e.get("URLParameters");
				List r = new ArrayList();
				for(int i=0; i<p.size(); i++) {
					JsonJavaObject o = (JsonJavaObject)p.get(i);
					String n = o.getString("name");
					if(uri.contains("{"+n+"}")) {
						r.add(o);
					}
				}
				if(!r.isEmpty()) {
					put(je,"uriParameters", r);
				}
			}
			put(je,"queryParameters", e.get("QueryParameters") );
			put(je,"headers", e.get("Headers") );
			
			// res_title is from the IBM documentation wiki only
			String resId = e.getString("res_title");
			if(StringUtil.isNotEmpty(resId)) {
				String docUrl = PathUtil.concat(client.getBaseUrl(), "/dx/"+resId, '/');
				// Fix when it comes from the staging server
				// ex: http://dwlhub.swg.usma.ibm.com/ldd/apiwiki.nsf/dx/Getting_the_All_Communities_feed_ic45
				//     http://www-10.lotus.com/ldd/appdevwiki.nsf/xpDocViewer.xsp?lookupName=IBM+Connections+4.5+API+Documentation#action=openDocument&res_title=Getting_the_All_Communities_feed_ic45&content=pdcontent
				//     http://www-10.lotus.com/ldd/ddwiki.nsf/dx/Database_collection_GET_dds10
				if(docUrl.startsWith("http://dwlhub.swg.usma.ibm.com/ldd/apiwiki.nsf/")) {
					docUrl = "http://www-10.lotus.com/ldd/appdevwiki.nsf/"+docUrl.substring("http://dwlhub.swg.usma.ibm.com/ldd/apiwiki.nsf/".length());
//				} else if(docUrl.startsWith("http://dwlhub.swg.usma.ibm.com/ldd/apiwiki.nsf/")) {
//					docUrl = "http://www-10.lotus.com/ldd/ddwiki.nsf/"+docUrl.substring("http://dwlhub.swg.usma.ibm.com/ldd/apiwiki.nsf/".length());
				}
				put(je,"doc_url",docUrl);
			} else {
				// Regular documentation path
				// The Playground should be accessed as HTTPS so we force it here
				String docUrl = PathUtil.concat(client.getBaseUrl(), "/ApiDocumentation.xsp#content=API&action=editDocument&documentId="+notesId, '/');
				if(docUrl.startsWith("http://")) {
					docUrl = "https://"+docUrl.substring(7);
				}
				put(je,"doc_url",docUrl);
			}
		}
		return je;
	}
	protected void put(JsonJavaObject o, String name, Object value) {
		if(value==null) {
			return;
		}
		if((value instanceof String) && StringUtil.isEmpty((String)value)) {
			return;
		}
		o.putObject(name, value);
	}

	private static class APIDocument {
		String[] products;
		String path;
		List<Object> content;
		public APIDocument(String[] products, String path) {
			this.products = products;
			this.path = path;
			this.content = new ArrayList<Object>();
		}
	}
	
	private static class DocEntry {
		String unid;
		String exPath;
		String title;
		public DocEntry(String unid, String exPath, String title) {
			this.unid = unid;
			this.exPath = exPath;
			this.title = title;
		}
		public String toString() {
			return StringUtil.format("Unid: {0}, path: {1}, title: {2}",unid,exPath,title);
		}
	}
	protected List<DocEntry> loadEntries(RestClient client, ImportSource source) throws IOException {
		// To use when the view will be categorized...
		String[] runtimes = source.getRuntimes();
		
		ArrayList<DocEntry> list = new ArrayList<APIImporter.DocEntry>();
		try {
			String path = URLEncoding.encodeURIString("/api/data/collections/name/AllAPIExplorer?ps=99999","utf-8",0,false);
			Object json = client.get(path,new RestClient.HandlerJson(JsonJavaObjectI.instanceExI)).getData();
			if(json instanceof List) {
				for(Object entry: (List)json) {
					if(entry instanceof Map) {
						Map m = (Map)entry;
						String unid = (String)m.get("@unid");
						String exPath = trimSeparator((String)m.get("APIExplorerPath"));
						if(exPath.indexOf('#')>=0) {
							exPath = exPath.substring(0,exPath.indexOf('#'));
						}
						String title = (String)m.get("Title");
						list.add(new DocEntry(unid,exPath,title));
					}
				}
			}
			return list;
		} catch(ClientServicesException ex) {
			throw new IOException(ex);
		}
	}
	protected JsonJavaObject loadAPIDocument(RestClient client, DocEntry entry) throws IOException {
		try {
			String path = URLEncoding.encodeURIString("/api/data/documents/unid/"+entry.unid,"utf-8",0,false);
			Object json = client.get(path,new RestClient.HandlerJson(JsonJavaObjectI.instanceExI)).getData();
			if(json instanceof JsonJavaObject) {
				// Parse the fields that have to be parsed, as they are stored as strings
				JsonJavaObject o = (JsonJavaObject)json;
				parse(o,"URLParameters");
				parse(o,"QueryParameters");
				parse(o,"Headers");
				parse(o,"RequestsDetails");
				return o;
			}
			return null;
		} catch(ClientServicesException ex) {
			throw new IOException(ex);
		}
	}
	protected void parse(JsonJavaObject o, String fieldName) throws IOException {
		// The conversion here is temp until D9
		fieldName = JsonJavaObjectI.convertKey(fieldName);
		
		Object f = o.get(fieldName);
		if(f instanceof String) {
			try {
				Object parsed = JsonParser.fromJson(JsonJavaFactory.instanceEx, (String)f);
				o.putObject(fieldName,parsed);
			} catch(JsonException jex) {
				o.remove(fieldName);
			}
		} else {
			//System.out.println("OK?");
		}
	}
	
	protected DominoClient createDominoClient(String baseUrl, String userName, String password) {
		return new DominoClient(baseUrl,userName,password);
	}
	public static class DominoClient extends RestClient {
		public DominoClient(String baseUrl, String userName, String password) {
			super(baseUrl);
			setAuthenticator(new BasicAuthenticator(userName,password));
		}
		// Allow HTTPS, regardless of the certificates
		@Override
		protected boolean isForceTrustSSLCertificate() throws ClientServicesException {
			return true;
		}
	}	
}
