package ge.jlbmw.jltranslate
import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class WhatsAppNotificationListener:NotificationListenerService() {
    private val recent=LinkedHashMap<String,Long>()
    override fun onNotificationPosted(sbn:StatusBarNotification?) {
        sbn?:return
        if(!Prefs.incoming(this)||sbn.packageName !in setOf("com.whatsapp","com.whatsapp.w4b"))return
        val n=sbn.notification?:return
        if(n.flags and Notification.FLAG_GROUP_SUMMARY!=0)return
        val text=(n.extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?:
            n.extras.getCharSequence(Notification.EXTRA_TEXT))?.toString()?.trim().orEmpty()
        val sender=n.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        if(text.isBlank()||sender.isBlank()||text.length>3000||TranslateEngine.isGeorgian(text) ||
            !text.any{it in 'a'..'z'||it in 'A'..'Z'})return
        val key=sbn.key+"|"+text;val now=System.currentTimeMillis()
        synchronized(recent){
            recent.entries.removeAll{now-it.value>300000}
            if(recent.containsKey(key))return
            recent[key]=now
            if(recent.size>100)recent.remove(recent.keys.first())
        }
        TranslateEngine.translate(text,false){r->r.onSuccess{showTranslation(sender,it,sbn)}}
    }
    private fun showTranslation(sender:String,translated:String,from:StatusBarNotification){
        if(!Prefs.incoming(this))return
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return
        val manager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel("translated","JL Translate • WhatsApp",NotificationManager.IMPORTANCE_DEFAULT))
        val b=Notification.Builder(this,"translated").setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle("თარგმანი • "+sender.take(70))
            .setContentText(translated.take(140))
            .setStyle(Notification.BigTextStyle().bigText(translated))
            .setVisibility(Notification.VISIBILITY_PRIVATE).setAutoCancel(true)
        from.notification.contentIntent?.let{b.setContentIntent(it)}
        manager.notify("jltranslate-"+from.key,7401,b.build())
    }
}
