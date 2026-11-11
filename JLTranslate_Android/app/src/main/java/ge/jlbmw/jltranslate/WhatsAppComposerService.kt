package ge.jlbmw.jltranslate
import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.Toast

class WhatsAppComposerService:AccessibilityService() {
    private val allowed=setOf("com.whatsapp","com.whatsapp.w4b")
    private var window:WindowManager?=null
    private var floating:Button?=null
    private var busy=false
    override fun onAccessibilityEvent(e:AccessibilityEvent?) {
        if(!Prefs.outgoing(this)){hide();return}
        val current=rootInActiveWindow?.packageName?.toString()
        if(current in allowed)show() else if(e?.eventType==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            e?.eventType==AccessibilityEvent.TYPE_WINDOWS_CHANGED)hide()
    }
    override fun onInterrupt(){hide()}
    override fun onDestroy(){hide();super.onDestroy()}
    private fun notice(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
    private fun show() {
        if(floating!=null)return
        try {
            val b=Button(this).apply {text="KA → EN";isAllCaps=false;setTextColor(Color.WHITE)
                setBackgroundColor(0xff176be7.toInt());setOnClickListener{translateNow()}}
            val p=WindowManager.LayoutParams(110,60,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT).apply{gravity=Gravity.RIGHT or Gravity.CENTER_VERTICAL;x=5;y=60}
            window=getSystemService(WINDOW_SERVICE) as WindowManager;window?.addView(b,p);floating=b
        }catch(_:Exception){floating=null}
    }
    private fun hide(){floating?.let{try{window?.removeView(it)}catch(_:Exception){}};floating=null}
    private fun findInput(n:AccessibilityNodeInfo?,depth:Int=0):AccessibilityNodeInfo? {
        if(n==null||depth>30)return null
        if(n.isEditable&&n.isVisibleToUser&&n.isEnabled)return n
        for(i in 0 until minOf(n.childCount,100)){findInput(n.getChild(i),depth+1)?.let{return it}}
        return null
    }
    private fun translateNow(){
        if(busy||!Prefs.outgoing(this))return
        val root=rootInActiveWindow?:return notice("WhatsApp ჩატი გახსენი")
        if(root.packageName?.toString() !in allowed)return
        val input=findInput(root)?:return notice("ჩასაწერი ველი ვერ მოიძებნა")
        val draft=input.text?.toString().orEmpty()
        if(!TranslateEngine.isGeorgian(draft))return notice("აკრიფე ქართული ასოებით")
        if(draft.length>3000)return notice("ტექსტი ძალიან გრძელია")
        busy=true;floating?.text="..."
        TranslateEngine.translate(draft,true){r->
            busy=false;floating?.text="KA → EN"
            r.onSuccess{en->
                val now=rootInActiveWindow
                val fresh=if(now?.packageName?.toString() in allowed)findInput(now) else null
                if(fresh?.text?.toString()!=draft)return@onSuccess notice("ტექსტი შეიცვალა")
                val args=Bundle().apply{putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,en)}
                if(fresh.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args))notice("ინგლისურად მზადაა — გადაამოწმე")
                else notice("WhatsApp-მა ჩანაცვლება დაბლოკა")
            }.onFailure{notice("თარგმნა ვერ მოხერხდა, ჩამოტვირთე მოდელები")}
        }
    }
}
