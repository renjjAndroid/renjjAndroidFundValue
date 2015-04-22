package com.renjj.fundvlaue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	//control
	//TextView txt = null;
	EditText addfundvalue = null;
	//Button send = null;
	Button add = null;
	Handler handler;
	ListView lstview = null;
	//基金购买净值
	EditText buyvalue = null;
	//基金份额
	EditText buyunit = null;
	//基金codelist
	private List<String> DayDayFundUrlLst = new ArrayList<String>(99);
	//基金购买净值list
	private Map<String,String> DayDayFundBuyMap = new HashMap<String,String>(99);
	//基金份额list
    private Map<String,String> DayDayFundUnitMap = new HashMap<String,String>(99);
	//返回的消息
	List<Map<String, Object>> data;
	//adpater
	//ArrayAdapter<String> ad = null; 
	//
	SimpleAdapter ad = null;
	private final String REDCSS = "<div id=\"statuspzgz\" class=\"fundpz\"><span class=\"red bold\">(.*?)</span>";
	private final String GREENCSS = "<div id=\"statuspzgz\" class=\"fundpz\"><span class=\"green bold\">(.*?)</span>";
	private final String BLACKCSS = "<div id=\"statuspzgz\" class=\"fundpz\"><span class=\"black bold\">(.*?)</span>";
		
	private Timer timer = null;  
	private TimerTask task;  
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getView();
		//send.setOnClickListener(this);
		add.setOnClickListener(this);
		//lstview.setDivider(getResources().getDrawable(R.drawable.fengexian));
		lstview.setDividerHeight(1);
		GetFundDataBySharePreferences();
		showFundLst();
		addfundvalue.setOnKeyListener(new OnKeyListener(){
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				//addfundvalue.setText("");
				return false;
			}
			
		});
	}
	
	private void getView(){
		//txt = (TextView)findViewById(R.id.fundvalue);
		//send = (Button)findViewById(R.id.send);
		add = (Button)findViewById(R.id.add);
		addfundvalue = (EditText)findViewById(R.id.addValue);
		lstview = (ListView)findViewById(R.id.listview);
		buyvalue = (EditText)findViewById(R.id.buyvalue);
		buyunit = (EditText)findViewById(R.id.buyunit);
	}
		
	private void Task() {	
    	task = new TimerTask() {  
    	    @Override  
    	    public void run() {  
    	    	 Message msg = new Message();  
    	    	 try {  
    	    		 data =  new ArrayList<Map<String, Object>>();
                	 for (int i= 0;i<DayDayFundUrlLst.size();i++){
                		 Map<String, Object> map =  new HashMap<String, Object>();
                		 map = getDaydayFundDate(DayDayFundUrlLst.get(i)); 
                		 data.add(map);
                         msg.what = data.size();  
             		}
                   
                 } catch (Exception e) {  
                     e.printStackTrace();  
                     msg.what = -1;  
                 }  
                 handler.sendMessage(msg);  
    	    }  
    	};   
    }  
	

    private Map<String, Object> getDaydayFundDate(String id) {  
    	String url = "http://fund.eastmoney.com/"+id+".html";
        String daydayString = http_get(url);  
        Pattern pgreen = Pattern.compile(GREENCSS);
        Pattern pred = Pattern.compile(REDCSS);
        Pattern pblack = Pattern.compile(BLACKCSS);
        Matcher mgreen = pgreen.matcher(daydayString);  
        Matcher mgred = pred.matcher(daydayString);  
        Matcher mgblack = pblack.matcher(daydayString);  
         
        Integer i=0;
        Map<String, Object> map = new HashMap<String, Object>(); 
        while (mgreen.find()) {  
            MatchResult mr=mgreen.toMatchResult();  
            map.put("valueStr", mr.group(1)+"↓");
            map.put("value", mr.group(1));
            Log.d("this", "---------------startGREEN----------------"+(++i));
            Log.d("this",  mr.group(1));
            Log.d("this", "---------------endGREEN----------------"+(i)); 
        }  
        
        while (mgred.find()) {  
            MatchResult mr=mgred.toMatchResult();
            map.put("valueStr", mr.group(1)+" ↑");
            map.put("value", mr.group(1));
            Log.d("this", "---------------startRED----------------"+(++i));
            Log.d("this",  mr.group(1));
            Log.d("this", "---------------endRED----------------"+(i)); 
        } 
        
        while (mgblack.find()) {  
            MatchResult mr=mgblack.toMatchResult();
            map.put("valueStr", mr.group(1)+" -");
            map.put("value", mr.group(1));
            Log.d("this", "---------------startBlack----------------"+(++i));
            Log.d("this",  mr.group(1));
            Log.d("this", "---------------endBlack----------------"+(i)); 
        } 
        
        //title_name
        String titlenamePattern = "<title>(.*?)</title>";
        Pattern pgtitle = Pattern.compile(titlenamePattern);
        Matcher mtitle = pgtitle.matcher(daydayString);
        
        while (mtitle.find()) {  
            MatchResult mr=mtitle.toMatchResult();  
            map.put("title", mr.group(1).replace("主页_天天基金网", "："));
            Log.d("this", "---------------starttitle----------------"+(++i));
            Log.d("this",  mr.group(1));
            Log.d("this", "---------------endtitle----------------"+(i)); 
        } 
              if(!map.containsKey("title")||!map.containsKey("value")){
            	  map.put("value", ",获取不到"+id+"基金的净值");
            	  map.put("title", "请确认是否输入正确");
            	   for (int j= 0;j<DayDayFundUrlLst.size();j++){
            		   if(id.equals(DayDayFundUrlLst.get(j))){
            			   DayDayFundUrlLst.remove(j);
            			   if(DayDayFundBuyMap.containsKey(DayDayFundUrlLst.get(j))){
            				   DayDayFundBuyMap.remove(DayDayFundUrlLst.get(j));
            			   }
            			   if(DayDayFundUnitMap.containsKey(DayDayFundUrlLst.get(j))){
            				   DayDayFundUnitMap.remove(DayDayFundUrlLst.get(j));
            			   }
            		   }
            	  }
              }
              
              if(DayDayFundBuyMap.containsKey(id+"buyVal")){
            	  map.put("fundBuyVal",DayDayFundBuyMap.get(id+"buyVal")); 
			   }
              if(DayDayFundUnitMap.containsKey(id+"unitVal")){
            	  map.put("fundUnitVal",DayDayFundUnitMap.get(id+"unitVal")); 
			   }
             
        return map;  
    }  
    
    //getResponse
    private String http_get(String url) {  
        final int RETRY_TIME = 3;  
        HttpClient httpClient = null;  
        HttpGet httpGet = null;  
  
        String responseBody = "";  
        int time = 0;  
        do {  
            try {  
                httpClient = getHttpClient();  
                httpGet = new HttpGet(url);  
                HttpResponse response = httpClient.execute(httpGet);  
                if (response.getStatusLine().getStatusCode() == 200) {  
                    //用gb2312编码转化为字符串  
                    byte[] bResult = EntityUtils.toByteArray(response.getEntity());  
                    if (bResult != null) {  
                        responseBody = new String(bResult,"gb2312");  
                    }  
                }  
                break;  
            } catch (IOException e) {  
                time++;  
                if (time < RETRY_TIME) {  
                    try {  
                        Thread.sleep(1000);  
                    } catch (InterruptedException e1) {  
                    }  
                    continue;  
                }  
                e.printStackTrace();  
            } finally {  
                httpClient = null;  
            }  
        } while (time < RETRY_TIME);  
  
        return responseBody;  
    }  
    
    private  HttpClient getHttpClient() {  
        HttpParams httpParams = new BasicHttpParams();  
        //设定连接超时和读取超时时间  
        HttpConnectionParams.setConnectionTimeout(httpParams, 6000);  
        HttpConnectionParams.setSoTimeout(httpParams, 30000);  
        return new DefaultHttpClient(httpParams);  
    }  
     
    private Handler getHandler() {  
        return new Handler(){  
            public void handleMessage(Message msg) {  
                if (msg.what < 0) {  
                    Toast.makeText(MainActivity.this, "数据获取失败", Toast.LENGTH_SHORT).show();  
                }else {  
                	if(data.size()>0){
                	  String [] res = new String[data.size()];                	  
                	  SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
                	  // ad资源
                	  List<Map<String, Object>> resMap = new ArrayList<Map<String, Object>>(){};
                	  for(int i=0;i<data.size();i++){
                		  Map<String, Object> map = data.get(i);
                		  String str = map.get("title").toString()+map.get("value").toString();
                		  str+= " 购买净值:"+map.get("fundBuyVal").toString()+" "+"购买数额:"+map.get("fundUnitVal").toString()+" "+df.format(new Date());
                		  res[i] = str;

                          //现在的计算
                          double value= Double.valueOf(map.get("value").toString());
                          double nowprice=0;
                          double unit =0;
                          if(!"".equals(map.get("fundUnitVal").toString())) {
                              unit = Double.valueOf(map.get("fundUnitVal").toString());
                              BigDecimal b = new BigDecimal(unit * value);
                              nowprice = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                          }

                          //购买的计算
                          double price=0;
                          double fundBuyVal =0;
                          if(!"".equals(map.get("fundBuyVal").toString())&&!"".equals
                                  (map.get("fundUnitVal").toString())) {
                              fundBuyVal = Double.valueOf(map.get("fundBuyVal").toString());
                              unit = Double.valueOf(map.get("fundUnitVal").toString());
                              BigDecimal b = new BigDecimal(unit * fundBuyVal);
                              price = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                          }

                          BigDecimal b = new BigDecimal(nowprice-price);
                          price = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                		  Map<String, Object> newMap = new HashMap<String, Object>(){};
                		  newMap.put("fundname", map.get("title").toString());
                		  newMap.put("fundtopinfo", "净值:"+map.get("valueStr").toString()+" 目前市值:"+nowprice+" 收益:"+price);
                		  newMap.put("fundmiddleinfo", "购买净值:"+fundBuyVal+" "+"数额:"+unit+" "+"市值:"+price);
                		  newMap.put("fundlowinfo", "更新时间:"+df.format(new Date()));
                		  resMap.add(newMap);
                	  }
                	  // 系统listview样式
                	  // ad= new ArrayAdapter<String>(MainActivity.this,R.layout.my_listitem,res);
                	  // 自定义简单listview样式
                	  // ad= new ArrayAdapter<String>(MainActivity.this,R.layout.my_listitem,R.id.item,res);
                	  // 自定义listview样式
                	  String[]from=new String[]{"fundname","fundtopinfo","fundmiddleinfo","fundlowinfo"};
                	  int[]to=new int[]{R.id.fundname,R.id.fundtopinfo,R.id.fundmiddleinfo,R.id.fundlowinfo};
                	  
                	  ad= new SimpleAdapter(MainActivity.this,resMap,R.layout.fundvalue_listitem,from,to);
                	  lstview.setAdapter(ad);
                	}
                }  
            }  
        };  
    }  
	
    //显示基金
    private void showFundLst(){
    	cleanSendTimerTask();
		timer = new Timer();  
		handler = getHandler();  
		Task();  
	    timer.schedule(task, 0, 50000);
    }
    
	@Override
	public void onClick(View v) {
		//添加基金
		if(v.getId() == R.id.add){
			//基金净值
			String fundvalue =addfundvalue.getText().toString();
			
			if(!"".equals(fundvalue)){
				//基金净值
				DayDayFundUrlLst.add(fundvalue);
				//购买基金净值
				String buyVal = buyvalue.getText().toString();
				//份额
				String unitVal = buyunit.getText().toString();
				
				Toast.makeText(MainActivity.this, "新增"+addfundvalue.getText().toString()+"成功~", Toast.LENGTH_SHORT).show();
				//购买基金净值
				DayDayFundBuyMap.put(addfundvalue.getText().toString()+"buyVal", buyVal);
				//份额
				DayDayFundUnitMap.put(addfundvalue.getText().toString()+"unitVal", unitVal);
				showFundLst();
				addfundvalue.setText("160625");
				buyvalue.setText("0");
				buyunit.setText("0");
			}
			else{
				Toast.makeText(MainActivity.this, "请输入6位基金代码", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void cleanSendTimerTask() {
         if (task != null) {
        	 task.cancel();
        	 task = null;
         }
         if (timer != null) {
        	 timer.cancel();
        	 timer.purge();
        	 timer = null;
         }
	 }
	
	
	 
	 //通过文件保存基金
	 private void GetFundDataBySharePreferences(){
		 SharedPreferences shareData = this.getSharedPreferences("fundData", MODE_PRIVATE);
		 String fundLst = shareData.getString("fundLst", "");
		 String[] funds = fundLst.split("#");
		 for (int i=0;i<funds.length;i++){
			DayDayFundUrlLst.add(funds[i]);
			 if(shareData.contains(funds[i]+"buyVal")){
				 DayDayFundBuyMap.put(funds[i]+"buyVal",shareData.getString(funds[i]+"buyVal", ""));
			 }
			 if(shareData.contains(funds[i]+"unitVal")){
				 DayDayFundUnitMap.put(funds[i]+"unitVal",shareData.getString(funds[i]+"unitVal", ""));
			 }
		 }	 
	 }
	 
	 //通过文件保存基金
	 private void SaveFundDataBySharePreferences(){
		 SharedPreferences.Editor shareData = this.getSharedPreferences("fundData", MODE_PRIVATE).edit();
		 String str ="";
		 for (int i= 0;i<DayDayFundUrlLst.size();i++){
			 str+=DayDayFundUrlLst.get(i)+"#";
			 //购买基金净值
			 if(DayDayFundBuyMap.containsKey(DayDayFundUrlLst.get(i)+"buyVal")){
				 shareData.putString(DayDayFundUrlLst.get(i)+"buyVal",DayDayFundBuyMap.get(DayDayFundUrlLst.get(i)+"buyVal"));
			 }
			 //购买基金份额
			 if(DayDayFundUnitMap.containsKey(DayDayFundUrlLst.get(i)+"unitVal")){
				 shareData.putString(DayDayFundUrlLst.get(i)+"unitVal",DayDayFundUnitMap.get(DayDayFundUrlLst.get(i)+"unitVal"));
			 }
		 }
		 shareData.putString("fundLst",str);
		
		 
		 shareData.commit();
	 }
	 
	 @Override
	protected void onDestroy() {
		 timer.cancel();  
		 cleanSendTimerTask();
		 SaveFundDataBySharePreferences(); 
		 super.onDestroy(); 
	}

	 @Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.main, menu);
			return true;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == R.id.action_settings) {
				return true;
			}
			else if (id == R.id.close){
				//关闭程序
				this.finish();
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
		
		
		
		
//		 //通过文件保存基金
//		 private void SaveFundData(){
//			 FileOutputStream fileOutputStream =null;
//			 OutputStreamWriter  outputStreamWriter = null;
//			 BufferedWriter bufferedWriter = null;
//			 try {
//				 String str = "";
//				 if(DayDayFundUrlLst.size()<=0){
//					 return;
//				 }
//				 for (int i= 0;i<DayDayFundUrlLst.size();i++){
//					 str+=DayDayFundUrlLst.get(i)+"#";
//				 }
//				 
//				 fileOutputStream = openFileOutput("fundData",MODE_PRIVATE);
//				 outputStreamWriter= new OutputStreamWriter(fileOutputStream);
//				 bufferedWriter = new BufferedWriter(outputStreamWriter);
//				 bufferedWriter.write(str);
//				 
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			 finally {
//				 try {
//				 if (bufferedWriter != null) {
//					 bufferedWriter.close();
//				 }
//				 if (outputStreamWriter != null) {
//					 outputStreamWriter.close();
//				 }
//				 if (fileOutputStream != null) {
//					 fileOutputStream.close();
//				 }
//				 } catch (IOException e) {
//				 e.printStackTrace();
//				 }
//			 }
//		 }
//		 
//		 //取出已经保存过的基金
//		 private void GetFundData(){
//			 FileInputStream fileInputStream =null;
//			 InputStreamReader  inputStreamReader = null;
//			 BufferedReader bufferedReader = null;
//			 try {
//				fileInputStream = openFileInput("fundData");
//				inputStreamReader = new InputStreamReader(fileInputStream);
//				bufferedReader = new BufferedReader(inputStreamReader);
//				String line = "";
//				StringBuilder content=new  StringBuilder();
//				while ((line = bufferedReader.readLine()) != null) {
//					content.append(line);
//				}
//				String[] funds = content.toString().split("#");
//				for (int i=0;i<funds.length;i++){
//					DayDayFundUrlLst.add(funds[i]);
//				}
//
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			 
//			 
//		 }
}
