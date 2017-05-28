package com.example.guncel_haberler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	Button btnGuncelle;
	ListView lv;
	private static String URL = "http://www.cumhuriyet.com.tr/rss/son_dakika.xml"; //son dakika haberlerine ait baþlý ve linklerin çekildiði XML sayfasý
	String [] basliklar = new String[20]; //baþýklarýn tutulduðu dizi
	String [] linkler = new String[20]; // linklerin tutulduðu dizi
	ArrayAdapter<String> veriAdaptoru; //listview adaptörü

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        new verileriCek().execute(); /* raporda bahsedilen paralel iþlemlerin gerçekeleþtirilmesini saðlamak için, 
        * bu kýsýmdan AsyncTask sýnýfýna ait doInBackground fonskiyon çaðrýlýp, çalýþtýrýlýyor. AsyncTask sýnýfý sayesinde
        * ana program etkilenmeden arkaplanda xml indirilip, ayrýþtýrma(parse) iþlemi gerçekleþtirilecektir. */
       
        lv = (ListView) findViewById(R.id.listView1); //haberler listviewe yüklendikten sonra, link view elemanlarý týklanabilir olacak.
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
				Toast.makeText(getApplicationContext(), "Haber içeriði yükleniyor...", Toast.LENGTH_SHORT).show();

				Bundle ekle = new Bundle();
				ekle.putString("anahtar", linkler[position].toString());
				//üzerine týklanan baþlýðýn linkini diðer modüle gönder...

				Intent goster = new Intent(MainActivity.this, HaberIcerik.class);
				goster.putExtras(ekle);
				startActivity(goster);
			}
        	 
		});
        
        btnGuncelle = (Button) findViewById(R.id.btn_guncelle);
        btnGuncelle.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new verileriCek().execute();
				Toast.makeText(getApplicationContext(), "Haberler güncel.", Toast.LENGTH_SHORT).show();
			}
		});
        
    }

    
    private class verileriCek extends AsyncTask<Void, Void, Void> { //xml parse iþleminin arkaplanda, farklý bir thread üzerinden gerçekleþmesini saðlayan AsyncTask sýnýfý.

        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
			Toast.makeText(getApplicationContext(), "Haber baþlýklarý yükleniyor...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
        	
        	try
            {	//xml dosyasýný arka planda indir:
            	Document doc  = Jsoup.connect(URL).get(); //XML dosyasý arka planda indiriliyor...
            	
            	//inen xml dosyasýný, istenen kritlere göre arkaplanda ayrýþtýr (parse et):
            	Elements titles = doc.select("title"); //xml dosyasýndaki title etiketlerini içerikleri ile beraber çek.
            	Elements links = doc.select("link"); //xml dosyasýndaki link etiketlerini içerikleri ile beraber çek.
            	 
            	int i=0, j=0;
            	
                for(Element title : titles) //"titles" adlý elements'e yüklenen "title" etiketini içeriði ile beraber "title" elementi aracýlýðýyla teker teker al. taa ki "titles" elementindeki etiketlerde yer alan tüm verileri çekene kadar.
                {
                	if(title.tagName().equals("title")) //içeriði ile beraber çekilen etiket gerçekten title mý? title ise içeriði al.
                	{
                		if(i < basliklar.length) //title'ýn içi boþ deðilse, diziye at.
                		{
                			if(j > 1)
                			{
                				basliklar[i] = title.text();  //çekilen title etiketine ait içeriði diziye at.
                				i++;
                			}
                			
                			else
                			{
                				j++; //dizinin indisini bir arttýrýyoruz. amaç, baþlýklarý bir diziye yüklemek. daha sonradan listview yüklenecek bu baþlýklar.
                			}
                		}
                	}
                }
                
                int m=0, n=0;
                for(Element link : links) //"links" adlý elements'e yüklenen "link" etiketi ile beraber içeriðini, "link" elementi aracýlýðýyla teker teker al. taa ki "links" elementindeki etiketlerde yer alan tüm verileri çekene kadar.
                {
                	if(link.select("link") != null) //içeriði ile beraber çekilen etiket gerçekten link etiketi mi? link etileti ise içeriði al.
                	{
                		if(m < linkler.length) ////link etiketinin içi boþ deðilse, diziye at.
                		{
                			if(n > 1)
                			{
                				linkler[m] = link.text(); //çekilen link etiketine ait içeriði diziye at.
                				m++;
                			}
                			
                			else
                			{
                				n++; //dizinin indisini bir arttýrýyoruz. amaç, baþlýklarý bir dizide tutup, listview'de hangi baþlýða týklanýrsa o baþlýða ait linki diðer modüle yollamak.
                			} //daha sonra o link aracýlýðýyla "HaberIcerik.java" modülünde, linkin açtýðý sayfadaki haberi çekeceðiz.
                		}  //baþlýk ve linkler farklý dizilerde tutulsalar bile farklý dizilerdeki baþlýk ve linklerin içerikleri ayný olacaktýr.
                	} //baþlýklarý dizisinin 1. indisindeki baþlýðýn linki, linkler dizisindeki 1. indiste yer alacaktýr.
                }
            }
            
            catch(Exception ex) //programýn geliþtirme aþamasýnda meydana gelebilecek olasý hatalar burada yazýlan ifadeler sayesinde logcat ekranýnda görüntülenebilir.
            {
            	Log.e("HATA : ", ex.toString());
            	Log.i("Bilgi : ", ex.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
        	listViewDoldur(basliklar,linkler); //doInBackground fonksiyonunun iþi bitince bu fonksiyon çalýþýyor ve listbox'un içini dolduruyor.
        	//böylece raporda bahsedilen modül-1'deki listview'in içi týklanabilir baþlýklarla dolduruluyor.
        }
    }
    
    public void listViewDoldur(String[] basliklar, String[] linkler){
    	//bu kýsýmdan listview içerisi haber baþlýklarý ile dolduruluyor.
    	veriAdaptoru = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, basliklar);
    	lv.setAdapter(veriAdaptoru);
    }
 }
