package com.zekrshomar.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.text.NumberFormat;
import java.util.*;

public class MainActivity extends Activity {
    static class Z {
        String title, arabic, sub; int goal; boolean custom;
        Z(String t, String a, String s, int g, boolean c){ title=t; arabic=a; sub=s; goal=g; custom=c; }
    }

    private final ArrayList<Z> defaults = new ArrayList<>();
    private final ArrayList<Z> customs = new ArrayList<>();
    private final String[][] fatima = new String[][]{
            {"الله اکبر", "اللَّهُ أَكْبَرُ", "34"},
            {"الحمدلله", "الْحَمْدُ لِلَّهِ", "33"},
            {"سبحان الله", "سُبْحَانَ اللَّهِ", "33"}
    };
    private SharedPreferences sp;
    private int index, count, step, total, completed;
    private boolean vibrate, finished;
    private TextView title, arabic, sub, counter, goal, stats, hint;
    private ProgressBar progress;
    private Button mainButton, vibButton;
    private NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("fa", "IR"));

    public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(13,102,91));
        sp = getSharedPreferences("zekr", MODE_PRIVATE);
        seedDefaults();
        loadCustoms();
        index = Math.max(0, Math.min(sp.getInt("index", 0), all().size()-1));
        count = sp.getInt("count", 0); step = sp.getInt("step", 0);
        total = sp.getInt("total", 0); completed = sp.getInt("completed", 0);
        vibrate = sp.getBoolean("vibrate", true); finished = sp.getBoolean("finished", false);
        buildUi(); updateUi();
    }

    private void seedDefaults(){
        defaults.clear();
        defaults.add(new Z("صلوات", "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَ آلِ مُحَمَّدٍ", "خدایا بر محمد و خاندان محمد درود فرست.", 100, false));
        defaults.add(new Z("استغفار", "أَسْتَغْفِرُ اللَّهَ رَبِّي وَ أَتُوبُ إِلَيْهِ", "از خداوند آمرزش می‌خواهم و به سوی او بازمی‌گردم.", 100, false));
        defaults.add(new Z("ذکر یونسیه", "لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ", "هیچ معبودی جز تو نیست؛ پاک و منزهی، من از ستمکاران بودم.", 313, false));
        defaults.add(new Z("لا حول و لا قوة", "لَا حَوْلَ وَ لَا قُوَّةَ إِلَّا بِاللَّهِ", "هیچ نیرو و توانی جز به کمک خدا نیست.", 100, false));
        defaults.add(new Z("ذکر فرج", "يَا اللَّهُ يَا رَحْمَٰنُ يَا رَحِيمُ يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ", "ای خدا، ای رحمان، ای رحیم؛ دلم را بر دینت ثابت بدار.", 100, false));
        defaults.add(new Z("سبحان الله", "سُبْحَانَ اللَّهِ", "خداوند پاک و منزه است.", 33, false));
        defaults.add(new Z("الحمدلله", "الْحَمْدُ لِلَّهِ", "ستایش مخصوص خداوند است.", 33, false));
        defaults.add(new Z("الله اکبر", "اللَّهُ أَكْبَرُ", "خداوند بزرگ‌تر است.", 34, false));
        defaults.add(new Z("لا اله الا الله", "لَا إِلَهَ إِلَّا اللَّهُ", "هیچ معبودی جز خدا نیست.", 100, false));
        defaults.add(new Z("حسبنا الله و نعم الوکیل", "حَسْبُنَا اللَّهُ وَ نِعْمَ الْوَكِيلُ", "خدا ما را کافی است و او بهترین کارساز است.", 100, false));
        defaults.add(new Z("یا الله", "یَا اللَّهُ", "ای خدا", 100, false));
        defaults.add(new Z("تسبیحات حضرت زهرا (س)", "اللَّهُ أَكْبَرُ", "سه مرحله: الله اکبر، الحمدلله، سبحان الله", 100, false));
    }

    private ArrayList<Z> all(){ ArrayList<Z> a = new ArrayList<>(); a.addAll(defaults); a.addAll(customs); return a; }
    private boolean isFatima(){ return get().title.startsWith("تسبیحات"); }
    private Z get(){ ArrayList<Z> a = all(); if(index<0 || index>=a.size()) index=0; return a.get(index); }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER); root.setPadding(34,28,34,24); root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); root.setBackgroundColor(Color.rgb(248,252,250));
        scroll.addView(root);
        title = tv(24, true, Color.rgb(20,60,55)); arabic = tv(28, true, Color.BLACK); sub = tv(15, false, Color.DKGRAY); counter = tv(70, true, Color.rgb(13,102,91)); goal = tv(16, false, Color.DKGRAY); stats = tv(14, false, Color.GRAY); hint = tv(13, false, Color.rgb(90,90,90));
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal); progress.setMax(100);
        root.addView(title, mp(-1,-2)); root.addView(arabic, mp(-1,-2)); root.addView(sub, mp(-1,-2)); root.addView(space(18)); root.addView(counter, mp(-1,-2)); root.addView(goal, mp(-1,-2)); root.addView(progress, mp(-1,18)); root.addView(stats, mp(-1,-2)); root.addView(hint, mp(-1,-2)); root.addView(space(16));
        mainButton = new Button(this); mainButton.setTextSize(24); mainButton.setTextColor(Color.WHITE); mainButton.setBackgroundColor(Color.rgb(13,102,91)); mainButton.setOnClickListener(v -> addOne()); mainButton.setOnLongClickListener(v -> { confirmMinus(); return true; }); root.addView(mainButton, mp(-1,150));
        LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER); row.setOrientation(LinearLayout.HORIZONTAL);
        Button pick = small("انتخاب ذکر"); pick.setOnClickListener(v -> pickZekr()); Button reset = small("بازنشانی"); reset.setOnClickListener(v -> confirmReset()); vibButton = small("لرزش"); vibButton.setOnClickListener(v -> { vibrate=!vibrate; save(); updateUi(); Toast.makeText(this, vibrate?"لرزش روشن شد":"لرزش خاموش شد", Toast.LENGTH_SHORT).show(); });
        row.addView(pick, mp(0,90,1)); row.addView(reset, mp(0,90,1)); row.addView(vibButton, mp(0,90,1)); root.addView(row, mp(-1,-2));
        LinearLayout row2 = new LinearLayout(this); row2.setGravity(Gravity.CENTER); row2.setOrientation(LinearLayout.HORIZONTAL);
        Button add = small("ذکر شخصی"); add.setOnClickListener(v -> editZekr(null, -1)); Button goalB = small("هدف دلخواه"); goalB.setOnClickListener(v -> editGoal()); Button history = small("آمار"); history.setOnClickListener(v -> showStats());
        row2.addView(add, mp(0,90,1)); row2.addView(goalB, mp(0,90,1)); row2.addView(history, mp(0,90,1)); root.addView(row2, mp(-1,-2));
        setContentView(scroll);
    }

    private TextView tv(int sp, boolean bold, int color){ TextView t=new TextView(this); t.setTextSize(sp); t.setTextColor(color); t.setGravity(Gravity.CENTER); t.setPadding(6,9,6,9); if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return t; }
    private Button small(String s){ Button b=new Button(this); b.setText(s); b.setTextSize(13); return b; }
    private View space(int h){ Space s=new Space(this); s.setLayoutParams(new LinearLayout.LayoutParams(1,h)); return s; }
    private LinearLayout.LayoutParams mp(int w,int h){ return new LinearLayout.LayoutParams(w,h); }
    private LinearLayout.LayoutParams mp(int w,int h,float weight){ return new LinearLayout.LayoutParams(w,h,weight); }

    private void addOne(){
        int target = target();
        if(finished || count >= target){ finished = true; save(); updateUi(); Toast.makeText(this,"این ذکر کامل شده است؛ برای شروع دوباره بازنشانی کنید.",Toast.LENGTH_SHORT).show(); return; }
        count++; total++; if (vibrate) ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(12);
        if(count >= target){
            if(isFatima() && step < 2){ step++; count=0; Toast.makeText(this,"مرحله بعدی: "+fatima[step][0],Toast.LENGTH_SHORT).show(); }
            else { completed++; finished=true; Toast.makeText(this,"ذکر شما کامل شد، قبول باشد",Toast.LENGTH_LONG).show(); if(vibrate)((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(70); }
        }
        save(); updateUi();
    }
    private void confirmMinus(){ new AlertDialog.Builder(this).setTitle("کسر یک ذکر").setMessage("یک واحد کم شود؟").setPositiveButton("بله",(d,w)->{ if(count>0){count--; finished=false;} save(); updateUi(); }).setNegativeButton("انصراف",null).show(); }
    private void confirmReset(){ new AlertDialog.Builder(this).setTitle("بازنشانی").setMessage("شمارنده ذکر فعلی از صفر شروع شود؟").setPositiveButton("بازنشانی",(d,w)->{count=0; step=0; finished=false; save(); updateUi();}).setNegativeButton("انصراف",null).show(); }

    private void pickZekr(){
        ArrayList<Z> a = all(); String[] names=new String[a.size()+1];
        for(int i=0;i<a.size();i++) names[i]=(a.get(i).custom?"✎ ":"")+a.get(i).title+"  — هدف "+nf.format(a.get(i).goal);
        names[a.size()]="+ ساخت ذکر شخصی جدید";
        new AlertDialog.Builder(this).setTitle("انتخاب ذکر").setItems(names,(d,which)->{
            if(which==a.size()) { editZekr(null,-1); return; }
            index=which; count=0; step=0; finished=false; save(); updateUi();
            if(get().custom) customOptions(which);
        }).show();
    }

    private void customOptions(int which){
        String[] ops = {"ویرایش ذکر شخصی", "حذف ذکر شخصی", "فقط انتخاب شد"};
        new AlertDialog.Builder(this).setTitle(get().title).setItems(ops,(d,o)->{
            if(o==0) editZekr(get(), which-defaults.size());
            if(o==1) confirmDelete(which-defaults.size());
        }).show();
    }
    private void confirmDelete(int customIndex){ if(customIndex<0 || customIndex>=customs.size())return; new AlertDialog.Builder(this).setTitle("حذف ذکر شخصی").setMessage("این ذکر حذف شود؟").setPositiveButton("حذف",(d,w)->{customs.remove(customIndex); index=0; count=0; step=0; finished=false; saveCustoms(); save(); updateUi();}).setNegativeButton("انصراف",null).show(); }

    private void editGoal(){
        if(isFatima()){ Toast.makeText(this,"هدف تسبیحات حضرت زهرا (س) مرحله‌ای و ثابت است.",Toast.LENGTH_SHORT).show(); return; }
        final EditText e = new EditText(this); e.setInputType(InputType.TYPE_CLASS_NUMBER); e.setText(String.valueOf(get().goal)); e.setSelectAllOnFocus(true); e.setGravity(Gravity.CENTER);
        new AlertDialog.Builder(this).setTitle("هدف دلخواه").setMessage("برای ذکر فعلی هدف جدید وارد کنید.").setView(e).setPositiveButton("ثبت",(d,w)->{ int g=parse(e.getText().toString(), get().goal); if(g>0){ get().goal=g; if(get().custom) saveCustoms(); count=0; finished=false; save(); updateUi(); } }).setNegativeButton("انصراف",null).show();
    }

    private void editZekr(Z initial, int customIndex){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(24,8,24,0);
        EditText t=input("عنوان", initial==null?"":initial.title, false); EditText a=input("متن ذکر", initial==null?"":initial.arabic, false); EditText s=input("ترجمه یا توضیح کوتاه", initial==null?"":initial.sub, false); EditText g=input("هدف", initial==null?"100":String.valueOf(initial.goal), true);
        box.addView(t); box.addView(a); box.addView(s); box.addView(g);
        new AlertDialog.Builder(this).setTitle(initial==null?"ذکر شخصی جدید":"ویرایش ذکر شخصی").setView(box).setPositiveButton("ذخیره",(d,w)->{
            String tt=t.getText().toString().trim(), aa=a.getText().toString().trim(), ss=s.getText().toString().trim(); int gg=parse(g.getText().toString(),100);
            if(tt.length()==0 || aa.length()==0 || gg<=0){ Toast.makeText(this,"عنوان، متن ذکر و هدف لازم است.",Toast.LENGTH_SHORT).show(); return; }
            Z z=new Z(tt,aa,ss,gg,true);
            if(customIndex>=0 && customIndex<customs.size()) customs.set(customIndex,z); else { customs.add(z); index=defaults.size()+customs.size()-1; }
            count=0; step=0; finished=false; saveCustoms(); save(); updateUi();
        }).setNegativeButton("انصراف",null).show();
    }
    private EditText input(String hint, String value, boolean number){ EditText e=new EditText(this); e.setHint(hint); e.setText(value); e.setSingleLine(false); e.setGravity(Gravity.RIGHT); if(number)e.setInputType(InputType.TYPE_CLASS_NUMBER); return e; }

    private void showStats(){ new AlertDialog.Builder(this).setTitle("آمار").setMessage("کل ذکرها: "+nf.format(total)+"\nهدف‌های کامل‌شده: "+nf.format(completed)+"\nذکر فعلی: "+get().title).setPositiveButton("باشه",null).setNegativeButton("پاک‌کردن",(d,w)-> new AlertDialog.Builder(this).setTitle("تأیید نهایی").setMessage("کل آمار پاک شود؟").setPositiveButton("پاک کن",(a,b)->{total=0;completed=0;save();updateUi();}).setNegativeButton("انصراف",null).show()).show(); }
    private int target(){ return isFatima() ? Integer.parseInt(fatima[step][2]) : get().goal; }
    private void updateUi(){
        Z z=get(); title.setText(z.title);
        if(isFatima()){ arabic.setText(fatima[step][1]); sub.setText("مرحله "+nf.format(step+1)+" از ۳: "+fatima[step][0]); }
        else { arabic.setText(z.arabic); sub.setText(z.sub); }
        counter.setText(nf.format(count)); goal.setText("هدف: "+nf.format(target())); progress.setProgress(Math.min(100, count*100/Math.max(1,target()))); stats.setText("کل ذکرها: "+nf.format(total)+"   |   تکمیل هدف: "+nf.format(completed));
        mainButton.setText(finished || count>=target()?"کامل شد — بازنشانی کنید":"ذکر بگو / شمارش"); mainButton.setEnabled(!(finished || count>=target())); vibButton.setText(vibrate?"لرزش روشن":"لرزش خاموش"); hint.setText("بعد از رسیدن به هدف، شمارش متوقف می‌شود. لمس طولانی: کم‌کردن یک عدد.");
    }
    private void save(){ sp.edit().putInt("index",index).putInt("count",count).putInt("step",step).putInt("total",total).putInt("completed",completed).putBoolean("vibrate",vibrate).putBoolean("finished",finished).apply(); }
    private int parse(String s,int def){ try{return Integer.parseInt(s.trim());}catch(Exception e){return def;} }
    private void loadCustoms(){ customs.clear(); try{ JSONArray arr=new JSONArray(sp.getString("customs","[]")); for(int i=0;i<arr.length();i++){ JSONObject o=arr.getJSONObject(i); customs.add(new Z(o.optString("title"),o.optString("arabic"),o.optString("sub"),o.optInt("goal",100),true)); } }catch(Exception ignored){} }
    private void saveCustoms(){ try{ JSONArray arr=new JSONArray(); for(Z z:customs){ JSONObject o=new JSONObject(); o.put("title",z.title); o.put("arabic",z.arabic); o.put("sub",z.sub); o.put("goal",z.goal); arr.put(o); } sp.edit().putString("customs",arr.toString()).apply(); }catch(Exception ignored){} }
}
