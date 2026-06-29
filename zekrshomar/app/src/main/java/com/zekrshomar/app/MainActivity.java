package com.zekrshomar.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.text.NumberFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final String[][] zekrs = new String[][]{
            {"صلوات", "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَ آلِ مُحَمَّدٍ", "خدایا بر محمد و خاندان محمد درود فرست.", "100"},
            {"استغفار", "أَسْتَغْفِرُ اللَّهَ رَبِّي وَ أَتُوبُ إِلَيْهِ", "از خداوند آمرزش می‌خواهم و به سوی او بازمی‌گردم.", "100"},
            {"سبحان الله", "سُبْحَانَ اللَّهِ", "خداوند پاک و منزه است.", "33"},
            {"الحمدلله", "الْحَمْدُ لِلَّهِ", "ستایش مخصوص خداوند است.", "33"},
            {"الله اکبر", "اللَّهُ أَكْبَرُ", "خداوند بزرگ‌تر است.", "34"},
            {"لا اله الا الله", "لَا إِلَهَ إِلَّا اللَّهُ", "هیچ معبودی جز خدا نیست.", "100"},
            {"حسبنا الله و نعم الوکیل", "حَسْبُنَا اللَّهُ وَ نِعْمَ الْوَكِيلُ", "خدا ما را کافی است و او بهترین کارساز است.", "100"},
            {"یا الله", "یَا اللَّهُ", "ای خدا", "100"},
            {"تسبیحات حضرت زهرا (س)", "اللَّهُ أَكْبَرُ", "مرحله ۱ از ۳: الله اکبر", "34"}
    };
    private final String[][] fatima = new String[][]{
            {"الله اکبر", "اللَّهُ أَكْبَرُ", "34"},
            {"الحمدلله", "الْحَمْدُ لِلَّهِ", "33"},
            {"سبحان الله", "سُبْحَانَ اللَّهِ", "33"}
    };
    private SharedPreferences sp;
    private int index, count, step, total, completed;
    private boolean vibrate;
    private TextView title, arabic, sub, counter, goal, stats;
    private ProgressBar progress;
    private NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("fa", "IR"));

    public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(13,102,91));
        sp = getSharedPreferences("zekr", MODE_PRIVATE);
        index = sp.getInt("index", 0); count = sp.getInt("count", 0); step = sp.getInt("step", 0);
        total = sp.getInt("total", 0); completed = sp.getInt("completed", 0); vibrate = sp.getBoolean("vibrate", true);
        buildUi(); updateUi();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER); root.setPadding(36,34,36,24); root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); root.setBackgroundColor(Color.rgb(248,252,250));
        title = tv(24, true, Color.rgb(20,60,55)); arabic = tv(29, true, Color.BLACK); sub = tv(16, false, Color.DKGRAY); counter = tv(72, true, Color.rgb(13,102,91)); goal = tv(16, false, Color.DKGRAY); stats = tv(14, false, Color.GRAY);
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal); progress.setMax(100);
        root.addView(title, mp(-1,-2)); root.addView(arabic, mp(-1,-2)); root.addView(sub, mp(-1,-2)); root.addView(space(20)); root.addView(counter, mp(-1,-2)); root.addView(goal, mp(-1,-2)); root.addView(progress, mp(-1,18)); root.addView(stats, mp(-1,-2)); root.addView(space(20));
        Button main = new Button(this); main.setText("ذکر بگو / شمارش"); main.setTextSize(24); main.setTextColor(Color.WHITE); main.setBackgroundColor(Color.rgb(13,102,91));
        main.setOnClickListener(v -> addOne()); main.setOnLongClickListener(v -> { confirmMinus(); return true; }); root.addView(main, mp(-1,150));
        LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER); row.setOrientation(LinearLayout.HORIZONTAL);
        Button pick = small("انتخاب ذکر"); pick.setOnClickListener(v -> pickZekr()); Button reset = small("بازنشانی"); reset.setOnClickListener(v -> confirmReset()); Button vib = small("لرزش: روشن"); vib.setOnClickListener(v -> { vibrate=!vibrate; save(); Toast.makeText(this, vibrate?"لرزش روشن شد":"لرزش خاموش شد", Toast.LENGTH_SHORT).show(); });
        row.addView(pick, mp(0,90,1)); row.addView(reset, mp(0,90,1)); row.addView(vib, mp(0,90,1)); root.addView(row, mp(-1,-2));
        Button history = small("آمار و تاریخچه"); history.setOnClickListener(v -> showStats()); root.addView(history, mp(-1,90));
        setContentView(root);
    }

    private TextView tv(int sp, boolean bold, int color){ TextView t=new TextView(this); t.setTextSize(sp); t.setTextColor(color); t.setGravity(Gravity.CENTER); t.setPadding(6,10,6,10); if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return t; }
    private Button small(String s){ Button b=new Button(this); b.setText(s); b.setTextSize(14); return b; }
    private View space(int h){ Space s=new Space(this); s.setLayoutParams(new LinearLayout.LayoutParams(1,h)); return s; }
    private LinearLayout.LayoutParams mp(int w,int h){ return new LinearLayout.LayoutParams(w,h); }
    private LinearLayout.LayoutParams mp(int w,int h,float weight){ return new LinearLayout.LayoutParams(w,h,weight); }

    private void addOne(){ int target = target(); int before = count; count++; total++; if (vibrate) ((android.os.Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(12); if(before < target && count >= target){ if(index==8 && step<2){ step++; count=0; Toast.makeText(this,"مرحله بعدی: "+fatima[step][0],Toast.LENGTH_SHORT).show(); } else { completed++; Toast.makeText(this,"ذکر شما کامل شد، قبول باشد",Toast.LENGTH_LONG).show(); if(vibrate)((android.os.Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(60); } } save(); updateUi(); }
    private void confirmMinus(){ new AlertDialog.Builder(this).setTitle("کسر یک ذکر").setMessage("یک واحد کم شود؟").setPositiveButton("بله",(d,w)->{ if(count>0)count--; save(); updateUi(); }).setNegativeButton("انصراف",null).show(); }
    private void confirmReset(){ new AlertDialog.Builder(this).setTitle("بازنشانی").setMessage("شمارنده از صفر شروع شود؟").setPositiveButton("بازنشانی",(d,w)->{count=0; step=0; save(); updateUi();}).setNegativeButton("انصراف",null).show(); }
    private void pickZekr(){ String[] names=new String[zekrs.length]; for(int i=0;i<zekrs.length;i++) names[i]=zekrs[i][0]+"  — هدف "+zekrs[i][3]; new AlertDialog.Builder(this).setTitle("انتخاب ذکر").setItems(names,(d,which)->{ index=which; count=0; step=0; save(); updateUi(); }).show(); }
    private void showStats(){ new AlertDialog.Builder(this).setTitle("آمار").setMessage("ذکرهای امروز/کل: "+nf.format(total)+"\nهدف‌های کامل‌شده: "+nf.format(completed)).setPositiveButton("باشه",null).setNegativeButton("پاک‌کردن",(d,w)-> new AlertDialog.Builder(this).setTitle("تأیید نهایی").setMessage("کل آمار پاک شود؟").setPositiveButton("پاک کن",(a,b)->{total=0;completed=0;save();updateUi();}).setNegativeButton("انصراف",null).show()).show(); }
    private int target(){ return index==8 ? Integer.parseInt(fatima[step][2]) : Integer.parseInt(zekrs[index][3]); }
    private void updateUi(){ title.setText(zekrs[index][0]); if(index==8){ arabic.setText(fatima[step][1]); sub.setText("مرحله "+nf.format(step+1)+" از ۳: "+fatima[step][0]); } else { arabic.setText(zekrs[index][1]); sub.setText(zekrs[index][2]); } counter.setText(nf.format(count)); goal.setText("هدف: "+nf.format(target())); progress.setProgress(Math.min(100, count*100/Math.max(1,target()))); stats.setText("کل ذکرها: "+nf.format(total)+"   |   تکمیل هدف: "+nf.format(completed)); }
    private void save(){ sp.edit().putInt("index",index).putInt("count",count).putInt("step",step).putInt("total",total).putInt("completed",completed).putBoolean("vibrate",vibrate).apply(); }
}
