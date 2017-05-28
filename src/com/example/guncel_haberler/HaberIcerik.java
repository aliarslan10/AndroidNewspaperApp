package com.example.guncel_haberler;

import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

@SuppressLint("NewApi") public class HaberIcerik extends Activity implements OnInitListener{

	Button btnSeslendir, btnDurdur; //haberi seslendirme (okuma) ve durdurma butonlar�
	TextView tv; //kullan�c�n�n i�eri�i okumas� ve motorun seslendirme yapmas� i�in, "icerikGoruntule" adl� string de�i�kenine att���m�z t�m i�erikleri daha sonradan textview'e aktar�yoruz ki i�erik kullan�c� taraf�ndan okunabilsin.
	String link; //di�er mod�lden gelen haberin likini al.
	String icerikGoruntule = ""; // �ekilen i�erik bu string'de tutulup ondan sonra textView'e aktar�lacak.
	TextToSpeech oku; //okuma i�lemi ger�ekle�tirecek olan TextToSpeech s�n�f�ndan bir nesne olu�turduk.
	
    @SuppressLint("JavascriptInterface") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
       
        try{
        	Bundle linkiAl = getIntent().getExtras(); // Kullan�c�n�n herhangi bir haberin ba�l���na t�klamas� sonucunda di�er mod�lden,
        	link = linkiAl.getString("anahtar");  // yani MainActivity.java'dan ald���m�z linki, "link" ad�nda bir de�i�kene at�yoruz. 
        										 // Ama�, JSoup ile indirme xml'i indirebilmek (�ekebilmek)
        	
        	new haberIcerigi().execute(); /* MainActivity.java'da oldu�u gibi  paralel i�lemlerin ger�ekele�tirilmesini sa�lamak i�in, 
            * bu k�s�mdan AsyncTask s�n�f�na ait doInBackground fonskiyon �a�r�l�p, �al��t�r�l�yor. AsyncTask s�n�f� sayesinde
            * ana program etkilenmeden arkaplanda xml indirilip, ayr��t�rma(parse) i�lemi ger�ekle�tirilecektir.  */
	    	
	    	ScrollView scroller = new ScrollView(this); //haber i�eri�i �ok uzun ise, uygulamada scroll �ubu�u aktif olsun.
	    	tv = (TextView) findViewById(R.id.tv);
	    	scroller.addView(tv);

    		}

			catch(Exception ex) //program�n geli�tirme a�amas�nda meydana gelebilecek olas� hatalar burada yaz�lan ifadeler sayesinde logcat ekran�nda g�r�nt�lenebilir.
			{
				Log.i("iiiiii : ", ex.toString());
				Log.e("eeeee : ", ex.toString());
				Log.d("dddd : ", ex.toString());
				Log.v("vvvvv : ", ex.toString());
				Log.w("wwwww : ", ex.toString());
			}
        
        
       btnSeslendir = (Button)findViewById(R.id.butonOku);
        
        btnSeslendir.setOnClickListener(new View.OnClickListener() {
			@Override
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
			// TODO Auto-generated method stub
				
				//TextToSpeech k�t�phanesi API 21 ve �ncesi apilerde speak 3 parametre al�rken, API 21'den sonra 4 parametre ald��� i�in if-else kullan�ld�.
				//yani, hangi api ile �al���rsak �al��al�m program sorunsuz bir �ekilde seslendirme yapacakt�r.
				if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) //Api 21 ve sonras� i�in.
				{ 
					oku.speak(tv.getText().toString(), TextToSpeech.QUEUE_ADD, null, null);
			    }
				
				else if(Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) //Api 21'den �ncesi i�in.
			    {
					oku.setLanguage(Locale.getDefault());
			        oku.speak(tv.getText().toString(), TextToSpeech.QUEUE_ADD, null);
			    }
				/* ��erik speak ile okunuyor. birinci parametre ile text al�n�yor, ikinci parametre ise her gelen
				 * yeni i�eri�i, okuma kuyru�una ekliyor. di�er parametre(veya yeni versionda parametreler) ise ses �zellikleri i�in. 
				 * mesela konu�ma h�z� ayar� gibi vs.*/
			}
		});
        
        btnDurdur = (Button)findViewById(R.id.butonDurdur);
        btnDurdur.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				oku.stop();
			}
		});

    }


	private class haberIcerigi extends AsyncTask<Void, Void, Void> { //xml parse i�leminin arkaplanda, farkl� bir thread �zerinden ger�ekle�mesini sa�layan AsyncTask s�n�f�.

        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

			try 
			{
				//xml dosyas�n� arka planda indir.
				Document doc = Jsoup.connect(link).get(); //XML dosyas� arka planda indiriliyor...
				
				//inen xml dosyas�n�, istenen kritlere g�re arkaplanda ayr��t�r (parse et)
				Elements baslik = doc.getElementsByTag("h1");//xml dosyas�ndaki 'h1' etiketlerini i�erikleri ile beraber �ek.
	        	Elements haber = doc.select("p"); //xml dosyas�ndaki 'p' etiketlerini i�erikleri ile beraber �ek.
	        	
	        	for(Element baslik_al : baslik) //"baslik" adl� elements'e y�klenen "h1" etiketi ile beraber i�eri�ini, "baslik_al" elementi arac�l���yla teker teker al. taa ki "baslik" elementindeki etiketlerde yer alan t�m verileri �ekene kadar.
	        	{
	        		icerikGoruntule = icerikGoruntule + "<h3>" +((baslik_al).toString()) + "</h3>";
	        	}
	        	
	        	for(Element icerik : haber) //"haber" adl� elements'e y�klenen "p" etiketi ile beraber i�eri�ini, "icerik" elementi arac�l���yla teker teker al. taa ki "haber" elementindeki etiketlerde yer alan t�m verileri �ekene kadar.
	        	{
	        		if(icerik.hasText())  //i�eri�i �ekilen etiket bo� de�ilse if'in i�ine gir.
	        		{
	        			icerikGoruntule = icerikGoruntule + ((icerik.text()).toString()) + "<br /> <br />";
	        		}
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
        	tv.setText(Html.fromHtml(icerikGoruntule)); //"icerikGoruntule" stringi i�indeki Html etiketlerinin textView'de yorumlanabilmesi i�in "Html.fromHtml" ifadesi kullan�ld�.

        	icerikGoruntule = "";
           
        	Intent intent = new Intent();
        	intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA); //ACTION_CHECK_TTS_DATA ifadesi intent arac�l���yla konu�ma aktivitisini ba�lat�yor.
        	startActivityForResult(intent,44); /* "intent" ile aktiviteyi aktif ediyoruz.
        	* "44", paketleme numaras�d�r. bunu biz belirliyoruz. integer bir de�erdir. 
        	* Bizi "ACTION_CHECK_TTS_DATA" ile verdi�imiz i� emrini bu "44" numaral� "ID"de tutuluyor. 
        	* intenet ile aktivite aktif olunca, "onActivityResult()" fonksiyonuna bir i� emri gidiyor. 
        	* onActivityResult() i�inde 44 numaras� kontrol ediliyor. 
        	* K�sacas� bizim atad���m�z ID numaras�d�r. ��nk� servislere �ok fazla emir gitmekte. Bu 44 numaral� "ID" ile
        	* bu iste�in, bizim okuma i�lemi yapan TextToSpeech kullan�lan "intent"ten geldi�ini anlayabiliyoruz.
        	*  onActivityResult() fonksiyonun 1. parametresi olan "requestCode" ile bu "44" numaras�n� al�yoruz. 
        	**/
        }
    }
	
    /*########################## SESLEND�RME ��LEMLER� ################################*/
	 /* 1. parametrede 44 ID'si requestCode i�inde kontrol ediliyor. TextToSpeech intentine ait bir veri mi diye kontrol ediliyor.
	  * 2. parametre, kontrol edilen 44 ID'sinin sonucunu d�nderiyor. Paket bo� mu yoksa dolu mu geldi diye... 
	  * 3. parametre intent aktivitisindeki veriyi al�yor. */	
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub			
		super.onActivityResult(requestCode, resultCode, data);						
		
		if(requestCode == 44) //text-to-speech �al���rken bu k�sma 44 ID'sini yolluyor. 
		{Log.i("REQUEST CODE : ", "�al���yor");
			if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) //44 kodu do�ru ise resultCode=1 oluyor. Yani data var.
			{
				oku = new TextToSpeech(this,this); //oku nesnesi olu�turuluyor.
			}
			
			else //veri bozuksa, yani resultCode=0 gelirse, o halde konu�ma giri�imi ba�ar�s�z olacak. 
			{ //Veri bozuk oldu�undan, yeni okuma giri�imi ba�latmam�z gerek. Onun i�in tekrardan intent olu�turuyoruz.
				Intent intent2 = new Intent();
				intent2.setAction(TextToSpeech.Engine.ACTION_TTS_DATA_INSTALLED);
				startActivity(intent2);
			}
		}
	}

    /* onInit metodu, konu�ma metodunun �al��mas� ile ilgili durumlar� bildirmek i�indir. Motorun �al��ma �n� ile ilgili durum mesajlar�n� buradan belirtebiliriz.
     * Mesela konu�ma motoru aktifse, "konu�a motoru me�gul" veya motorun i�i bitti�inde "konu�ma i�lemi sona erdi" tarz�nda (toast s�n�f ile vs. bu uyar�lar verdirilebilir)
     * bilgilendirme mesajlar� vermek amac�yla kullan�labilir. OnInitListener ifadesini implement etti�imiz i�in, "onInit" fonksiyonunu kullanm�yor olsak bile
     * bu fonksiyonu bu �ekilde override etmek zorunday�z. aksi takdirde uygulama hata verecektir. ��nk� motor tetiklendi�i anda buraya bir olay d�nderilir. */
	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
	}
}

		
