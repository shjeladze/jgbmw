package ge.jlbmw.jltranslate

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var entry:EditText
    private lateinit var result:TextView
    private lateinit var status:TextView
    private var english = true
    private var translated = ""
    private fun msg(s:String) = Toast.makeText(this,s,Toast.LENGTH_LONG).show()
    private fun label(s:String,big:Int=16):TextView = TextView(this).apply {
        text=s; textSize=big.toFloat(); setTextColor(0xff162235.toInt()); setPadding(0,15,0,15)
    }
    private fun button(parent:LinearLayout,s:String, f:()->Unit):Button {
        val b=Button(this); b.text=s; b.isAllCaps=false; b.setOnClickListener{f()}
        parent.addView(b,LinearLayout.LayoutParams(-1,-2)); return b
    }
    override fun onCreate(savedInstanceState:Bundle?) { super.onCreate(savedInstanceState)
        val scroll=ScrollView(this); scroll.setBackgroundColor(0xfff5f7fb.toInt())
        val lay=LinearLayout(this);lay.orientation=1; lay.setPadding(28,26,28,26)
        scroll.addView(lay);setContentView(scroll)
        lay.addView(label("JL Translate",30))
        lay.addView(label("WhatsApp • ქართული ↔ English"))
        lay.addView(label("1. ორმხრივი თარგმანი",21))
        val mode=button(lay,"ქართული → English"){ english=!english
            modeText(); entry.setText("");translated="";result.text="თარგმანი აქ გამოჩნდება" }
        modeButton=mode
        entry=EditText(this); entry.hint="დაწერე ტექსტი...";entry.minLines=3;entry.maxLines=6
        entry.gravity=Gravity.TOP;lay.addView(entry)
        button(lay,"თარგმნე"){ val t=entry.text.toString()
            if(t.isBlank()){msg("შეიყვანე ტექსტი");return@button}
            result.text="ითარგმნება..."
            TranslateEngine.translate(t,english){r->runOnUiThread {
                r.onSuccess{ translated=it;result.text=it }
                 .onFailure{ result.text="შეცდომა: ჯერ ჩამოტვირთე ენის მოდელები Wi-Fi-ით" }
            }}
        }
        result=label("თარგმანი აქ გამოჩნდება",18)
        result.setBackgroundColor(0xffe9f0ff.toInt());lay.addView(result)
        lay.addView(label("Powered by Google Translate",12))
        button(lay,"თარგმანის კოპირება"){ if(translated.isBlank())msg("ჯერ თარგმნე")
            else { (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager)
                .setPrimaryClip(ClipData.newPlainText("JL Translate",translated));msg("დაკოპირებულია") }}
        button(lay,"გაგზავნა WhatsApp-ით"){ if(translated.isBlank())msg("ჯერ თარგმნე")
            else startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{
                type="text/plain"; putExtra(Intent.EXTRA_TEXT,translated)},"აირჩიე WhatsApp"))}
        lay.addView(label("2. WhatsApp-ის ავტომატური რეჟიმი",21))
        status=label("");lay.addView(status)
        button(lay,"ენის მოდელების ჩამოტვირთვა (Wi-Fi)"){status.text="იტვირთება..."
            TranslateEngine.download{ok->runOnUiThread{msg(if(ok)"ენები მზადაა" else "ჩამოტვირთვა ვერ მოხერხდა")
                status.text=if(ok)"ენები ჩამოტვირთულია" else "ჩართე Wi-Fi და სცადე ხელახლა"}}}
        button(lay,"შემოსული ინგლისური შეტყობინებების თარგმნა"){ AlertDialog.Builder(this)
            .setTitle("შეტყობინებების ნებართვა")
            .setMessage("Android-ის Notification Access-ის ჩართვისას აპს შეუძლია შეტყობინებების წაკითხვა. მხოლოდ WhatsApp/WhatsApp Business-ის პრევიუებს თარგმნის ამ ტელეფონზევე. ახალ ქართულ თარგმანს ცალკე შეტყობინებად აჩვენებს. ჩართო?")
            .setNegativeButton("არა",null).setPositiveButton("ჩართვა"){_,_->
                Prefs.setIncoming(this,true)
                if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
                    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),10)
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }.show() }
        button(lay,"KA → EN ღილაკი WhatsApp-ის ჩატში"){AlertDialog.Builder(this)
            .setTitle("Accessibility ნებართვა")
            .setMessage("ეს მგრძნობიარე ნებართვა აპს ეკრანის ტექსტზე წვდომას აძლევს. აპი ღილაკს მხოლოდ WhatsApp-ში აჩვენებს; მასზე დაჭერისას წაიკითხავს გაუგზავნელ ტექსტს, თარგმნის და ჩაანაცვლებს. Send-ს ავტომატურად არ აჭერს. ჩართო?")
            .setNegativeButton("არა",null).setPositiveButton("ვეთანხმები"){_,_->
                Prefs.setOutgoing(this,true);startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }.show()}
        button(lay,"ავტომატური რეჟიმების გამორთვა"){Prefs.setIncoming(this,false);Prefs.setOutgoing(this,false)
            status.text="გამორთულია აპში. Android Settings-შიც შეგიძლია ნებართვების გაუქმება";msg("გამორთულია")}
        lay.addView(label("WhatsApp-ის ორიგინალი მიმოწერა არ იცვლება. შემოსული თარგმანი ცალკე შეტყობინებაა; გასაგზავნი ტექსტი ჯერ გადაამოწმე. ძველი ჩატების წაკითხვა შეუძლებელია. პირველი ჩამოტვირთვის შემდეგ თარგმანი ლოკალურად მუშაობს. ეს არ არის WhatsApp-ის ოფიციალური აპი.",13))
    }
    private var modeButton:Button?=null
    private fun modeText(){modeButton?.text=if(english)"ქართული → English" else "English → ქართული"}
    override fun onResume(){super.onResume();if(::status.isInitialized){
        val n=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationOk=if(Build.VERSION.SDK_INT>=27)n.isNotificationListenerAccessGranted(ComponentName(this,WhatsAppNotificationListener::class.java)) else false
        val accessibility=Settings.Secure.getString(contentResolver,Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?.contains(packageName,ignoreCase=true) ?: false
        status.text="შემოსული: "+if(Prefs.incoming(this)&&notificationOk)"ჩართულია" else "ნებართვა აკლია ან გამორთულია"
        status.append("\nKA → EN: "+if(Prefs.outgoing(this)&&accessibility)"ჩართულია" else "ნებართვა აკლია ან გამორთულია")
    }}
}
