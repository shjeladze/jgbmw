package ge.jlbmw.marketing;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.ClipData;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.util.Base64;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> uploadCallback;
    private static final int PICK_FILE=9001;
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        webView=new WebView(this);
        WebSettings settings=webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        webView.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri target=request.getUrl();
                if("file".equals(target.getScheme()) && target.toString().startsWith("file:///android_asset/")) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW,target)); } catch(Exception ignored) {}
                return true;
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.startsWith("file:///android_asset/")) return false;
                try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception ignored){}
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if(uploadCallback!=null){uploadCallback.onReceiveValue(null);}
                uploadCallback=callback;
                try { startActivityForResult(params.createIntent(),PICK_FILE); }
                catch(Exception ex){uploadCallback=null;callback.onReceiveValue(null);Toast.makeText(MainActivity.this,"ფაილის არჩევა ვერ გაიხსნა",Toast.LENGTH_SHORT).show();}
                return true;
            }
        });
        webView.addJavascriptInterface(new JsBridge(),"JLNative");
        webView.loadUrl("file:///android_asset/index.html");
        setContentView(webView);
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==PICK_FILE && uploadCallback!=null){
            uploadCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode,data));
            uploadCallback=null;
        }
    }
    public final class JsBridge {
        @JavascriptInterface public void compose(String to,String subject,String body,String dataImage) {
            runOnUiThread(()->{
                try {
                    Intent send;
                    boolean hasImage=dataImage!=null && dataImage.startsWith("data:image/") && dataImage.contains(",");
                    if(hasImage){
                        String prefix=dataImage.substring(0,dataImage.indexOf(','));
                        String ext=prefix.contains("png")?"png":"jpg";
                        byte[] bytes=Base64.decode(dataImage.substring(dataImage.indexOf(',')+1),Base64.DEFAULT);
                        if(bytes.length>3*1024*1024)throw new IllegalArgumentException("Image too large");
                        File dir=new File(getCacheDir(),"share");dir.mkdirs();
                        File file=new File(dir,"jl-programmer-banner."+ext);
                        try(FileOutputStream output=new FileOutputStream(file)){output.write(bytes);}
                        Uri shared=FileProvider.getUriForFile(MainActivity.this,"ge.jlbmw.marketing.fileprovider",file);
                        send=new Intent(Intent.ACTION_SEND);
                        send.setType(ext.equals("png")?"image/png":"image/jpeg");
                        send.putExtra(Intent.EXTRA_STREAM,shared);
                        send.setClipData(ClipData.newRawUri("JL Programmer",shared));
                        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }else {
                        send=new Intent(Intent.ACTION_SENDTO);
                        send.setData(Uri.parse("mailto:"+Uri.encode(to)));
                    }
                    send.putExtra(Intent.EXTRA_EMAIL,new String[]{to});
                    send.putExtra(Intent.EXTRA_SUBJECT,subject);
                    send.putExtra(Intent.EXTRA_TEXT,body);
                    startActivity(Intent.createChooser(send,"Gmail ან სხვა Email აპი"));
                }catch(Exception err){
                    Toast.makeText(MainActivity.this,"Gmail ვერ გაიხსნა: "+err.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }
        @JavascriptInterface public void openGmail() {
            runOnUiThread(()->{
                try{
                    Intent i=getPackageManager().getLaunchIntentForPackage("com.google.android.gm");
                    if(i==null)i=new Intent(Intent.ACTION_VIEW,Uri.parse("https://mail.google.com/"));
                    startActivity(i);
                }catch(Exception ignored){Toast.makeText(MainActivity.this,"Gmail ვერ გაიხსნა",Toast.LENGTH_SHORT).show();}
            });
        }
    }
    @Override public void onBackPressed(){
        if(webView!=null && webView.canGoBack())webView.goBack(); else super.onBackPressed();
    }
    @Override protected void onDestroy(){if(webView!=null)webView.destroy();super.onDestroy();}
}
