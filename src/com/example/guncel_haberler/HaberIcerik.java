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

	Button btnSeslendir, btnDurdur; //haberi seslendirme (okuma) ve durdurma butonlarý
	TextView tv; //kullanýcýnýn içeriði okumasý ve motorun seslendirme yapmasý için, "icerikGoruntule" adlý string deðiþkenine attýðýmýz tüm içerikleri daha sonradan textview'e aktarýyoruz ki içerik kullanýcý tarafýndan okunabilsin.
	String link; //diðer modülden gelen haberin likini al.
	String icerikGoruntule = ""; // çekilen içerik bu string'de tutulup ondan sonra textView'e aktarýlacak.
	TextToSpeech oku; //okuma iþlemi gerçekleþtirecek olan TextToSpeech sýnýfýndan bir nesne oluþturduk.
	
    @SuppressLint("JavascriptInterface") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
       
        try{
        	Bundle linkiAl = getIntent().getExtras(); // Kullanýcýnýn herhangi bir haberin baþlýðýna týklamasý sonucunda diðer modülden,
        	link = linkiAl.getString("anahtar");  // yani MainActivity.java'dan aldýðýmýz linki, "link" adýnda bir deðiþkene atýyoruz. 
        										 // Amaç, JSoup ile indirme xml'i indirebilmek (çekebilmek)
        	
        	new haberIcerigi().execute(); /* MainActivity.java'da olduðu gibi  paralel iþlemlerin gerçekeleþtirilmesini saðlamak için, 
            * bu kýsýmdan AsyncTask sýnýfýna ait doInBackground fonskiyon çaðrýlýp, çalýþtýrýlýyor. AsyncTask sýnýfý sayesinde
            * ana program etkilenmeden arkaplanda xml indirilip, ayrýþtýrma(parse) iþlemi gerçekleþtirilecektir.  */
	    	
	    	ScrollView scroller = new ScrollView(this); //haber içeriði çok uzun ise, uygulamada scroll çubuðu aktif olsun.
	    	tv = (TextView) findViewById(R.id.tv);
	    	scroller.addView(tv);

    		}

			catch(Exception ex) //programýn geliþtirme aþamasýnda meydana gelebilecek olasý hatalar burada yazýlan ifadeler sayesinde logcat ekranýnda görüntülenebilir.
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
				
				//TextToSpeech kütüphanesi API 21 ve öncesi apilerde speak 3 parametre alýrken, API 21'den sonra 4 parametre aldýðý için if-else kullanýldý.
				//yani, hangi api ile çalýþýrsak çalýþalým program sorunsuz bir þekilde seslendirme yapacaktýr.
				if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) //Api 21 ve sonrasý için.
				{ 
					oku.speak(tv.getText().toString(), TextToSpeech.QUEUE_ADD, null, null);
			    }
				
				else if(Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) //Api 21'den öncesi için.
			    {
					oku.setLanguage(Locale.getDefault());
			        oku.speak(tv.getText().toString(), TextToSpeech.QUEUE_ADD, null);
			    }
				/* Ýçerik speak ile okunuyor. birinci parametre ile text alýnýyor, ikinci parametre ise her gelen
				 * yeni içeriði, okuma kuyruðuna ekliyor. diðer parametre(veya yeni versionda parametreler) ise ses özellikleri için. 
				 * mesela konuþma hýzý ayarý gibi vs.*/
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


	private class haberIcerigi extends AsyncTask<Void, Void, Void> { //xml parse iþleminin arkaplanda, farklý bir thread üzerinden gerçekleþmesini saðlayan AsyncTask sýnýfý.

        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

			try 
			{
				//xml dosyasýný arka planda indir.
				Document doc = Jsoup.connect(link).get(); //XML dosyasý arka planda indiriliyor...
				
				//inen xml dosyasýný, istenen kritlere göre arkaplanda ayrýþtýr (parse et)
				Elements baslik = doc.getElementsByTag("h1");//xml dosyasýndaki 'h1' etiketlerini içerikleri ile beraber çek.
	        	Elements haber = doc.select("p"); //xml dosyasýndaki 'p' etiketlerini içerikleri ile beraber çek.
	        	
	        	for(Element baslik_al : baslik) //"baslik" adlý elements'e yüklenen "h1" etiketi ile beraber içeriðini, "baslik_al" elementi aracýlýðýyla teker teker al. taa ki "baslik" elementindeki etiketlerde yer alan tüm verileri çekene kadar.
	        	{
	        		icerikGoruntule = icerikGoruntule + "<h3>" +((baslik_al).toString()) + "</h3>";
	        	}
	        	
	        	for(Element icerik : haber) //"haber" adlý elements'e yüklenen "p" etiketi ile beraber içeriðini, "icerik" elementi aracýlýðýyla teker teker al. taa ki "haber" elementindeki etiketlerde yer alan tüm verileri çekene kadar.
	        	{
	        		if(icerik.hasText())  //içeriði çekilen etiket boþ deðilse if'in içine gir.
	        		{
	        			icerikGoruntule = icerikGoruntule + ((icerik.text()).toString()) + "<br /> <br />";
	        		}
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
        	tv.setText(Html.fromHtml(icerikGoruntule)); //"icerikGoruntule" stringi içindeki Html etiketlerinin textView'de yorumlanabilmesi için "Html.fromHtml" ifadesi kullanýldý.

        	icerikGoruntule = "";
           
        	Intent intent = new Intent();
        	intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA); //ACTION_CHECK_TTS_DATA ifadesi intent aracýlýðýyla konuþma aktivitisini baþlatýyor.
        	startActivityForResult(intent,44); /* "intent" ile aktiviteyi aktif ediyoruz.
        	* "44", paketleme numarasýdýr. bunu biz belirliyoruz. integer bir deðerdir. 
        	* Bizi "ACTION_CHECK_TTS_DATA" ile verdiðimiz iþ emrini bu "44" numaralý "ID"de tutuluyor. 
        	* intenet ile aktivite aktif olunca, "onActivityResult()" fonksiyonuna bir iþ emri gidiyor. 
        	* onActivityResult() içinde 44 numarasý kontrol ediliyor. 
        	* Kýsacasý bizim atadýðýmýz ID numarasýdýr. Çünkü servislere çok fazla emir gitmekte. Bu 44 numaralý "ID" ile
        	* bu isteðin, bizim okuma iþlemi yapan TextToSpeech kullanýlan "intent"ten geldiðini anlayabiliyoruz.
        	*  onActivityResult() fonksiyonun 1. parametresi olan "requestCode" ile bu "44" numarasýný alýyoruz. 
        	**/
        }
    }
	
    /*########################## SESLENDÝRME ÝÞLEMLERÝ ################################*/
	 /* 1. parametrede 44 ID'si requestCode içinde kontrol ediliyor. TextToSpeech intentine ait bir veri mi diye kontrol ediliyor.
	  * 2. parametre, kontrol edilen 44 ID'sinin sonucunu dönderiyor. Paket boþ mu yoksa dolu mu geldi diye... 
	  * 3. parametre intent aktivitisindeki veriyi alýyor. */	
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub			
		super.onActivityResult(requestCode, resultCode, data);						
		
		if(requestCode == 44) //text-to-speech çalýþýrken bu kýsma 44 ID'sini yolluyor. 
		{Log.i("REQUEST CODE : ", "Çalýþýyor");
			if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) //44 kodu doðru ise resultCode=1 oluyor. Yani data var.
			{
				oku = new TextToSpeech(this,this); //oku nesnesi oluþturuluyor.
			}
			
			else //veri bozuksa, yani resultCode=0 gelirse, o halde konuþma giriþimi baþarýsýz olacak. 
			{ //Veri bozuk olduðundan, yeni okuma giriþimi baþlatmamýz gerek. Onun için tekrardan intent oluþturuyoruz.
				Intent intent2 = new Intent();
				intent2.setAction(TextToSpeech.Engine.ACTION_TTS_DATA_INSTALLED);
				startActivity(intent2);
			}
		}
	}

    /* onInit metodu, konuþma metodunun çalýþmasý ile ilgili durumlarý bildirmek içindir. Motorun çalýþma âný ile ilgili durum mesajlarýný buradan belirtebiliriz.
     * Mesela konuþma motoru aktifse, "konuþa motoru meþgul" veya motorun iþi bittiðinde "konuþma iþlemi sona erdi" tarzýnda (toast sýnýf ile vs. bu uyarýlar verdirilebilir)
     * bilgilendirme mesajlarý vermek amacýyla kullanýlabilir. OnInitListener ifadesini implement ettiðimiz için, "onInit" fonksiyonunu kullanmýyor olsak bile
     * bu fonksiyonu bu þekilde override etmek zorundayýz. aksi takdirde uygulama hata verecektir. Çünkü motor tetiklendiði anda buraya bir olay dönderilir. */
	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
	}
}

		
