package com.nuplay.oneproaplicador;

import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.content.pm.*;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.*;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.widget.*;
import org.xmlpull.v1.XmlPullParser;
import android.util.Xml;
import java.util.*;
import java.io.*;

public class MainActivity extends Activity {
 TextView status; Button next; ArrayList<ResolveInfo> apps = new ArrayList<>(); HashMap<String,String> mappings = new HashMap<>(); int index=0; String iconPackage=""; Resources iconResources; PackageManager pm;
 @Override public void onCreate(Bundle b){ super.onCreate(b); pm=getPackageManager(); LinearLayout l=new LinearLayout(this); l.setPadding(32,40,32,20); l.setOrientation(1); l.setBackgroundColor(Color.rgb(16,18,19)); TextView title=new TextView(this); title.setText("OnePro Dark Aplicador"); title.setTextColor(Color.WHITE); title.setTextSize(24); l.addView(title); status=new TextView(this); status.setTextColor(Color.WHITE); status.setTextSize(16); status.setPadding(0,24,0,24); l.addView(status); next=new Button(this); next.setText("Preparar e adicionar próximo atalho"); l.addView(next); TextView warning=new TextView(this); warning.setText("A Samsung pode pedir confirmação para cada atalho. Os ícones originais não são alterados. Você pode remover os atalhos antigos manualmente."); warning.setTextColor(Color.LTGRAY); l.addView(warning); setContentView(l); next.setOnClickListener(v->addNext()); load(); }
 void load(){try{Intent i=new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_LAUNCHER); apps.addAll(pm.queryIntentActivities(i,0)); Collections.sort(apps,(a,b)->a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString())); for(PackageInfo pkg:pm.getInstalledPackages(0)){try{Resources r=pm.getResourcesForApplication(pkg.packageName); int id=r.getIdentifier("appfilter","xml",pkg.packageName); if(id==0)continue; HashMap<String,String> m=new HashMap<>(); XmlPullParser p=r.getXml(id); for(int e=p.getEventType();e!=XmlPullParser.END_DOCUMENT;e=p.next()){if(e==XmlPullParser.START_TAG && "item".equals(p.getName())){String component=p.getAttributeValue(null,"component"), drawable=p.getAttributeValue(null,"drawable"); if(component!=null && drawable!=null){int start=component.indexOf('{'),end=component.indexOf('/'); if(start>=0 && end>start)m.put(component.substring(start+1,end),drawable);}}} if(m.size()>100){mappings=m;iconPackage=pkg.packageName;iconResources=r;break;}}catch(Exception ignored){}} status.setText(iconPackage.isEmpty()?"Pacote de ícones compatível não encontrado. Instale o OnePro Dark e abra novamente.":"Pacote encontrado: "+iconPackage+"\nAplicativos detectados: "+apps.size()+"\nToque para preparar o primeiro atalho.");next.setEnabled(!iconPackage.isEmpty());}catch(Exception e){status.setText("Erro: "+e.getMessage());}}
 void addNext(){ShortcutManager manager=getSystemService(ShortcutManager.class);if(manager==null || !manager.isRequestPinShortcutSupported()){status.setText("O launcher atual não aceita a criação de atalhos fixados por este método.");return;} while(index<apps.size()){ResolveInfo app=apps.get(index++); String pkg=app.activityInfo.packageName; String drawable=mappings.get(pkg); if(drawable==null)continue; int resId=iconResources.getIdentifier(drawable,"drawable",iconPackage);if(resId==0)continue; try{Drawable d=iconResources.getDrawable(resId,null);Bitmap bitmap=Bitmap.createBitmap(192,192,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bitmap);d.setBounds(0,0,192,192);d.draw(c);Intent launch=new Intent(Intent.ACTION_MAIN);launch.addCategory(Intent.CATEGORY_LAUNCHER);launch.setComponent(new ComponentName(pkg,app.activityInfo.name));launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);String name=app.loadLabel(pm).toString();ShortcutInfo shortcut=new ShortcutInfo.Builder(this,"onepro_"+pkg+"_"+index).setShortLabel(name.length()>40?name.substring(0,40):name).setIcon(Icon.createWithBitmap(bitmap)).setIntent(launch).build();boolean requested=manager.requestPinShortcut(shortcut,null);status.setText((requested?"Solicitação enviada: ":"Não foi possível solicitar: ")+name+"\nProgresso: "+index+"/"+apps.size()+"\nConfirme a inclusão na tela inicial e toque novamente para continuar.");return;}catch(Exception ignored){}}status.setText("Todos os aplicativos com ícones correspondentes foram processados.");next.setEnabled(false); }
}
