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
	private static String URL = "http://www.cumhuriyet.com.tr/rss/son_dakika.xml"; //son dakika haberlerine ait ba�l� ve linklerin �ekildi�i XML sayfas�
	String [] basliklar = new String[20]; //ba��klar�n tutuldu�u dizi
	String [] linkler = new String[20]; // linklerin tutuldu�u dizi
	ArrayAdapter<String> veriAdaptoru; //listview adapt�r�

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        new verileriCek().execute(); /* raporda bahsedilen paralel i�lemlerin ger�ekele�tirilmesini sa�lamak i�in, 
        * bu k�s�mdan AsyncTask s�n�f�na ait doInBackground fonskiyon �a�r�l�p, �al��t�r�l�yor. AsyncTask s�n�f� sayesinde
        * ana program etkilenmeden arkaplanda xml indirilip, ayr��t�rma(parse) i�lemi ger�ekle�tirilecektir. */
       
        lv = (ListView) findViewById(R.id.listView1); //haberler listviewe y�klendikten sonra, link view elemanlar� t�klanabilir olacak.
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
				Toast.makeText(getApplicationContext(), "Haber i�eri�i y�kleniyor...", Toast.LENGTH_SHORT).show();

				Bundle ekle = new Bundle();
				ekle.putString("anahtar", linkler[position].toString());
				//�zerine t�klanan ba�l���n linkini di�er mod�le g�nder...

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
				Toast.makeText(getApplicationContext(), "Haberler g�ncel.", Toast.LENGTH_SHORT).show();
			}
		});
        
    }

    
    private class verileriCek extends AsyncTask<Void, Void, Void> { //xml parse i�leminin arkaplanda, farkl� bir thread �zerinden ger�ekle�mesini sa�layan AsyncTask s�n�f�.

        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
			Toast.makeText(getApplicationContext(), "Haber ba�l�klar� y�kleniyor...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
        	
        	try
            {	//xml dosyas�n� arka planda indir:
            	Document doc  = Jsoup.connect(URL).get(); //XML dosyas� arka planda indiriliyor...
            	
            	//inen xml dosyas�n�, istenen kritlere g�re arkaplanda ayr��t�r (parse et):
            	Elements titles = doc.select("title"); //xml dosyas�ndaki title etiketlerini i�erikleri ile beraber �ek.
            	Elements links = doc.select("link"); //xml dosyas�ndaki link etiketlerini i�erikleri ile beraber �ek.
            	 
            	int i=0, j=0;
            	
                for(Element title : titles) //"titles" adl� elements'e y�klenen "title" etiketini i�eri�i ile beraber "title" elementi arac�l���yla teker teker al. taa ki "titles" elementindeki etiketlerde yer alan t�m verileri �ekene kadar.
                {
                	if(title.tagName().equals("title")) //i�eri�i ile beraber �ekilen etiket ger�ekten title m�? title ise i�eri�i al.
                	{
                		if(i < basliklar.length) //title'�n i�i bo� de�ilse, diziye at.
                		{
                			if(j > 1)
                			{
                				basliklar[i] = title.text();  //�ekilen title etiketine ait i�eri�i diziye at.
                				i++;
                			}
                			
                			else
                			{
                				j++; //dizinin indisini bir artt�r�yoruz. ama�, ba�l�klar� bir diziye y�klemek. daha sonradan listview y�klenecek bu ba�l�klar.
                			}
                		}
                	}
                }
                
                int m=0, n=0;
                for(Element link : links) //"links" adl� elements'e y�klenen "link" etiketi ile beraber i�eri�ini, "link" elementi arac�l���yla teker teker al. taa ki "links" elementindeki etiketlerde yer alan t�m verileri �ekene kadar.
                {
                	if(link.select("link") != null) //i�eri�i ile beraber �ekilen etiket ger�ekten link etiketi mi? link etileti ise i�eri�i al.
                	{
                		if(m < linkler.length) ////link etiketinin i�i bo� de�ilse, diziye at.
                		{
                			if(n > 1)
                			{
                				linkler[m] = link.text(); //�ekilen link etiketine ait i�eri�i diziye at.
                				m++;
                			}
                			
                			else
                			{
                				n++; //dizinin indisini bir artt�r�yoruz. ama�, ba�l�klar� bir dizide tutup, listview'de hangi ba�l��a t�klan�rsa o ba�l��a ait linki di�er mod�le yollamak.
                			} //daha sonra o link arac�l���yla "HaberIcerik.java" mod�l�nde, linkin a�t��� sayfadaki haberi �ekece�iz.
                		}  //ba�l�k ve linkler farkl� dizilerde tutulsalar bile farkl� dizilerdeki ba�l�k ve linklerin i�erikleri ayn� olacakt�r.
                	} //ba�l�klar� dizisinin 1. indisindeki ba�l���n linki, linkler dizisindeki 1. indiste yer alacakt�r.
                }
            }
            
            catch(Exception ex) //program�n geli�tirme a�amas�nda meydana gelebilecek olas� hatalar burada yaz�lan ifadeler sayesinde logcat ekran�nda g�r�nt�lenebilir.
            {
            	Log.e("HATA : ", ex.toString());
            	Log.i("Bilgi : ", ex.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
        	listViewDoldur(basliklar,linkler); //doInBackground fonksiyonunun i�i bitince bu fonksiyon �al���yor ve listbox'un i�ini dolduruyor.
        	//b�ylece raporda bahsedilen mod�l-1'deki listview'in i�i t�klanabilir ba�l�klarla dolduruluyor.
        }
    }
    
    public void listViewDoldur(String[] basliklar, String[] linkler){
    	//bu k�s�mdan listview i�erisi haber ba�l�klar� ile dolduruluyor.
    	veriAdaptoru = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, basliklar);
    	lv.setAdapter(veriAdaptoru);
    }
 }
