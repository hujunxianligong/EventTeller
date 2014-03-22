package index.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import util.Const;

import com.alibaba.fastjson.JSON;

import config.JsonConfigModel;
import config.LocalJsonConfigReader;
import db.hbn.model.Event;

public class EventIndex {
	
	
	private static String solrUrl;
	
	public EventIndex(){
		String fileContent = LocalJsonConfigReader.readJsonFile(Const.SYS_JSON_CONFIG_PATH);
		JsonConfigModel jcm = JSON.parseObject(fileContent,JsonConfigModel.class);
		solrUrl = jcm.SolrIndexURI;
	}
	
	
	public void update(List<Event> events){
		try{
			SolrServer server = new HttpSolrServer(solrUrl);
			List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
			for(Event et : events){
				SolrInputDocument doc = new SolrInputDocument();
				doc.addField("id", et.getId());
				doc.addField("et_title", et.getTitle());
				doc.addField("et_summary", et.getContent().substring(0, Math.min(et.getContent().length(), 100)).replace("!##!", "\n"));
				doc.addField("et_pubTime", et.getPubTime());
				docs.add(doc);
			}
			server.add(docs);
			server.commit();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("can't update to solr...");
		}
	}
	
	public void update(Set<Event> events){
		try{
			SolrServer server = new HttpSolrServer(solrUrl);
			List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
			for(Event et : events){
				SolrInputDocument doc = new SolrInputDocument();
				doc.addField("id", et.getId());
				doc.addField("et_title", et.getTitle());
				doc.addField("et_summary", et.getContent().substring(0, Math.min(et.getContent().length(), 200)).replace("!##!", "\n"));
				doc.addField("et_pubTime", et.getPubTime());
				doc.addField("et_number", et.getNumber());
				docs.add(doc);
			}
			server.add(docs);
			server.commit();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("can't update to solr...");
		}
	}
	
	public void update(Event et){
		try{
			SolrServer server = new HttpSolrServer(solrUrl);
			List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id", et.getId());
			doc.addField("et_title", et.getTitle());
			doc.addField("et_summary", et.getContent().substring(0, Math.min(et.getContent().length(), 100)));
			doc.addField("et_pubTime", et.getPubTime());
			docs.add(doc);
			server.add(docs);
			server.commit();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("can't update to solr...");
		}
	}
	
	public List<Integer> queryIds(String queryStr,int start, int num,String sort,String order){
		List<Integer> res = new ArrayList<Integer>();
		SolrServer server = new HttpSolrServer(solrUrl);
		SolrQuery query =new SolrQuery();  
        query.setQuery(queryStr);
		query.setStart(start);
		query.setRows(num);
		if(sort != null){
			if(order.equals("asc")){
				query.setSort(sort, SolrQuery.ORDER.asc);		
			}else{
				query.setSort(sort, SolrQuery.ORDER.desc);
			}
		}
		try {
			QueryResponse response = server.query(query);
			SolrDocumentList docs = response.getResults();
			for (SolrDocument doc : docs) { 
				int id = Integer.parseInt(doc.getFieldValue("id").toString());
				res.add(id);
			}
		}catch(Exception e){
			return res;
		}
		return res;
	}
	
	@SuppressWarnings("deprecation")
	public List<Event> queryEvents(String queryStr,int start, int num,String sort,String order){
		List<Event> res = new ArrayList<Event>();
		SolrServer server = new HttpSolrServer(solrUrl);
		SolrQuery query =new SolrQuery();  
        query.setQuery(queryStr);
		query.setStart(start);
		query.setRows(num);
		if(sort != null){
			if(order.equals("asc")){
				query.setSort(sort, SolrQuery.ORDER.asc);		
			}else{
				query.setSort(sort, SolrQuery.ORDER.desc);
			}
		}
		try {
			QueryResponse response = server.query(query);
			SolrDocumentList docs = response.getResults();
			for (SolrDocument doc : docs) { 
				int id = Integer.parseInt(doc.getFieldValue("id").toString());
				String title = doc.getFieldValue("et_title").toString();
				String time = doc.getFieldValue("et_pubTime").toString();
				String summary = doc.getFieldValue("et_summary").toString();
				String number = doc.getFieldValue("et_number").toString();
//				int topic = Integer.parseInt(doc.getFieldValue("et_topicId").toString());
				Event et = new Event();
				et.setId(id);
				et.setTitle(title);
				et.setPubTime(new Date(time));
				et.setContent(summary);
				et.setNumber(Integer.parseInt(number));
				res.add(et);
			}
		}catch(Exception e){
			return res;
		}
		return res;
	}
	
	public void deleteItem(String id){
		SolrServer server = new HttpSolrServer(solrUrl);
		try {
			server.deleteById(id);
			server.commit();
		} catch (SolrServerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteAll(){
		SolrServer server = new HttpSolrServer(solrUrl);
		String queryStr = "*:*";
		try {
			server.deleteByQuery(queryStr);
			server.commit();
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
	}

}
