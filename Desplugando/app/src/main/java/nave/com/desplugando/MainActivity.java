package nave.com.desplugando;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class MainActivity extends AppCompatActivity implements Runnable,ServiceConnection
{
    ViciService mService;
    final ServiceConnection mConnection = this;
    Intent intent;
    boolean mBound = false;
    LinearLayout ll ;
    Handler h ;
    TextView fb,wpp,insta,twitter;
    Button selectapp,removebt,usototal,config;
    RelativeLayout uso,total,configuration,father;
    RadioButton dia,seman;
    ImageView SplashScreen;
    List <apptocheck> AppsList;
    String PackToADD= null;
    boolean diary;
    boolean  done;
    int splashtime;
    List<Integer>toDoImages;
    List<String>toDoTitles;
    ImageView barrinha ;
    int controlercor;
    String[] InitialApps = new String[] {"com.facebook.katana","com.whatsapp","com.twitter.android","com.instagram.android"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflator = (LayoutInflater) this .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.image, null);

        actionBar.setCustomView(v);
        intent = new Intent(this,ViciService.class);
        ServiceStart();
        h= new Handler();
        h.post(this);
        done = false;
        ll = (LinearLayout) findViewById(R.id.ll);
        father= (RelativeLayout)findViewById(R.id.activity_main);
        SharedPreferences sp = getSharedPreferences("prefs", Activity.MODE_PRIVATE);
        diary = sp.getBoolean("diary",true);
        SplashScreen= (ImageView)findViewById(R.id.SplashScreen);
        SplashScreen.setAlpha(1f);
        SplashScreen.setVisibility(View.VISIBLE);
        splashtime=0;
        barrinha = (ImageView) findViewById(R.id.Barrinha);
        String applist =sp.getString("apps",null);
        if (applist==null)
        {
        AppsList = new ArrayList<>() ;
        for (int i=0;i<InitialApps.length;i++)
        {
            controlercor=sp.getInt("cor",0);
            controlercor++;
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("cor",controlercor);
            editor.commit();
            if (isPackageExisted(InitialApps[i]))
            {
                Drawable icon = null;
                try {
                    icon = getPackageManager().getApplicationIcon(InitialApps[i]);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                AppsList.add(new apptocheck(InitialApps[i],0,"false","false",0));
                addnewRelative(ll,icon,InitialApps[i],0);
            }
        }
            if (mBound)
            {   mService.AppsList= AppsList;
                debug("manda p la ");
            }

        }
        else {
            String serialized;
            List<apptocheck> tempora= new ArrayList<>();
            serialized= sp.getString("apps",null);
            if (serialized!=null)
            {

                String[] classes = serialized.split("!");
               classes = sort(classes);


                for (int i=0;i<classes.length;i++)
                {
                    debug("Ira Deserializar o "+classes[i]);

                    String[]a = classes[i].split("°");
                    if (isPackageExisted(a[0])){
                    if (a[2]==null)a[2]= "false";
                    if (a[3]==null)a[3]="false";

                    tempora.add( new apptocheck(a[0], Integer.parseInt(a[1]),a[2],a[3], Integer.parseInt(a[4])));
                }
                }
                AppsList= tempora;

                for(int i=0;i<AppsList.size();i++)
                {
                    Drawable icon =null ;
                    try {
                     icon = getPackageManager().getApplicationIcon(AppsList.get(i).packagename);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    controlercor = sp.getInt("cor",0);
                    controlercor++;
                    addnewRelative(ll,icon,AppsList.get(i).packagename,AppsList.get(i).useTime);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("cor",controlercor);
                    editor.commit();
                }
            }

        }
        if (!isAccessGranted()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Você deve permitir que o aplicativo tenha acesso a os aplicativos em background para continar" )
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            startActivity(intent);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        }

        }
        String[] sort (String[]toSort)
        {
            int[] values= new int[toSort.length];
            String[]Organized= new String[toSort.length];
            for(int i =0;i<toSort.length;i++)
            {
                String[]a = toSort[i].split("°");
                values[i]= Integer.parseInt(a[1]);
            }
            values = BubbleSort(values);
            for (int i=0;i<values.length;i++)
            {
                Organized[i]=GetfromArray(toSort,values[i],Organized);
            }
            return Organized;
        }
    String GetfromArray(String[]toFind,int whatis,String[]current)
    {
        for(String s : toFind)
        {
            String[] a = s.split("°");
            if (Integer.parseInt(a[1])==whatis)
            {
                int control=0;
                for (int i=0;i<current.length;i++)
                {
                   if ( !Arrays.asList(current).contains(s)){
                       return s;
                   }


                }

        }
        }
        return null;
    }
    private boolean isAccessGranted() {

        if (Build.VERSION.SDK_INT>=19){
            try {
        PackageManager packageManager = getPackageManager();
        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
        AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = 0;
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
            mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);
        }
        return (mode == AppOpsManager.MODE_ALLOWED);

    } catch (PackageManager.NameNotFoundException e) {
        return false;
    }}
    return  true ;
}



    public static void debug(String s)
    {
        Log.d("vicio", s);
    }
    @Override
    protected  void onResume()
    {
        super.onResume();
        ServiceStart();
    }
    @Override
    protected  void onPause()
    {
       super.onPause();
        unbindService(this);
        mBound= false;
        debug("Discnect");
    }

    @Override
    protected void onStart() {

        SharedPreferences share = getSharedPreferences("prefs", Activity.MODE_PRIVATE);
        final SharedPreferences.Editor editore=  share.edit();

        removebt= (Button)findViewById(R.id.Remove);
        dia = (RadioButton)findViewById(R.id.Diariamente);
        seman= (RadioButton)findViewById(R.id.Semanal);
        selectapp= (Button)findViewById(R.id.MonitorarApp);
        usototal= (Button)findViewById(R.id.totalbt);


        config= (Button) findViewById(R.id.configsbt);
        uso = (RelativeLayout)findViewById(R.id.UsoLayout);
        total= (RelativeLayout)findViewById(R.id.UsoTotal);
        configuration=(RelativeLayout)findViewById(R.id.Configs);
        barrinha.setImageResource(R.drawable.bar2);
        uso.setVisibility(View.VISIBLE);
        total.setVisibility(View.INVISIBLE);
        configuration.setVisibility(View.INVISIBLE);
        toDoTitles= new ArrayList<>();
        toDoImages = new ArrayList<>();
        Integer[] temparray= {R.drawable.caminahr,R.drawable.conversar,R.drawable.esportes,R.drawable.museu,R.drawable.praia,R.drawable.surf};
        toDoImages.addAll(Arrays.asList( temparray));
        String[] temptitles = {"Caminhar","conversar com amigos","praticar esporte","ir ao museu","ir a praia","ir surfar"};
        toDoTitles.addAll(Arrays.asList(temptitles));

        usototal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tempValue =0;
                if (mBound) {
                    for(apptocheck apps: mService.AppsList)
                    {
                        tempValue+=apps.useTime;
                    }
                    TextView Totaluse = (TextView)findViewById(R.id.Totaluse);
                    Totaluse.setText(CriadordeHorario(tempValue));
                    CreateWhatDo((LinearLayout)findViewById(R.id.doLayout),tempValue);
                    uso.setVisibility(View.INVISIBLE);
                    total.setVisibility(View.VISIBLE);
                    configuration.setVisibility(View.INVISIBLE);
                    barrinha.setImageResource(R.drawable.bar1);
                }

            }
        });


        config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uso.setVisibility(View.INVISIBLE);
                configuration.setVisibility(View.VISIBLE);
                total.setVisibility(View.INVISIBLE);
                barrinha.setImageResource(R.drawable.bar3);
            }
        });
        findViewById(R.id.backuso).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uso.setVisibility(View.VISIBLE);
                configuration.setVisibility(View.INVISIBLE);
                total.setVisibility(View.INVISIBLE);
                barrinha.setImageResource(R.drawable.bar2);
            }
        });
        selectapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent appIntent;

                appIntent=new Intent(view.getContext(),Chooser.class);
                startActivityForResult(appIntent, 1);
            }
        });
        if (diary)
        {
            dia.setChecked(true);
            seman.setChecked(false);
        }
        else {
            dia.setChecked(false);
            seman.setChecked(true);
        }
        dia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dia.setChecked(true);
                seman.setChecked(false);
                diary= true ;
                editore.putBoolean("diary",diary);
                editore.commit();
            }
        });
        seman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dia.setChecked(false);
                seman.setChecked(true);
                diary= false;
                editore.putBoolean("diary",diary);
                editore.commit();
            }
        });
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
// ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.cpicker, null);
        final Spinner sp= (Spinner) dialogView.findViewById(R.id.spinner);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (sp.getSelectedItem().toString()!=null)
                {
                    if (mBound){
                    String[] bala =getappspackage(mService.AppsList);
                    debug(bala[sp.getSelectedItemPosition()]);
                        apptocheck apptoremove= null;
                        for (apptocheck aps:mService.AppsList) {
                            if (aps.packagename.equals(bala[sp.getSelectedItemPosition()]))
                            {
                                apptoremove= aps;
                                break;
                            }

                        }
                        mService.AppsList.remove(apptoremove);
                        for(int z =0;z<ll.getChildCount();z++)
                        {

                            if (ll.getChildAt(z) instanceof RelativeLayout)
                            {
                                RelativeLayout rl =(RelativeLayout) ll.getChildAt(z);
                                for (int w =0;w<rl.getChildCount();w++)
                                {
                                    if (rl.getChildAt(w) instanceof TextView) {
                                        if (( rl.getChildAt(w)).getVisibility() == View.INVISIBLE) {
                                           if (((TextView)rl.getChildAt(w)).getText().equals(apptoremove.packagename))
                                           {
                                               ll.removeView(rl);
                                              // doRestart(getApplicationContext());
                                           }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }).setCancelable(true).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        final AlertDialog alertDialog = dialogBuilder.create();

        removebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                String[] bala =getAppsName(mService.AppsList);
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, bala); //selected item will look like a spinner set from XML
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp.setAdapter(spinnerArrayAdapter);
                    if (bala.length!=0) {
                        alertDialog.show();
                    }
                    else Toast.makeText(getBaseContext(),"Você não pode remover as redes socias iniciais",Toast.LENGTH_LONG).show();
            }
            }
        });

        if (PackToADD!=null)
        {


            uso.setVisibility(View.VISIBLE);

        }

        super.onStart();
    }
    String[] getappspackage(List<apptocheck>l)
    {
        List<String >temp = new ArrayList<>();
        if (l!=null)
        {
            for (apptocheck app:l  ) {
                int counter=0;
                for(int i =0;i<InitialApps.length;i++) {
                    if (!app.packagename.equals(InitialApps[i])) {
                        counter++;

                    }
                }if (counter==4)
                {
                    temp.add(app.packagename);
                }


            }
        }
        return temp.toArray(new String[0]);
    }
    String[] getAppsName(List<apptocheck> l)
    {
        List<String >temp = new ArrayList<>();
        if (l!=null)
        {
            for (apptocheck app:l  ) {
                int counter=0;
               for(int i =0;i<InitialApps.length;i++)
               {
                  if (!app.packagename.equals(InitialApps[i]))
                   { counter++;
               }
               if (counter==4)
               {
                   PackageManager packageManager= getApplicationContext().getPackageManager();
                   String appName = null; try {
                   appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(app.packagename, PackageManager.GET_META_DATA));
               } catch (PackageManager.NameNotFoundException e) {
                   e.printStackTrace();
               }
                   temp.add(appName);

               }
               }

            }
        }
        return temp.toArray(new String[0]);
    }

    public static int[] BubbleSort( int[] arrayToBeSorted)
    {
        int auxNumber = 0;
        for (int i = 0; i < arrayToBeSorted.length; i++)
        {
            for (int j = 0; j < arrayToBeSorted.length - 1; j++)
            {
                if (arrayToBeSorted[j] > arrayToBeSorted[j + 1])
                {
                    auxNumber = arrayToBeSorted[j];
                    arrayToBeSorted[j] = arrayToBeSorted[j + 1];
                    arrayToBeSorted[j + 1] = auxNumber;
                }
            }
        }
        return arrayToBeSorted;
    }
    boolean checkIfisMonitoring(String s,List<apptocheck> list)

    {
        for (apptocheck ap:list
             ) {if (ap.packagename.equals( s))
            return true;
        }
        return false;
    }
    @Override
    public void onBackPressed()
    {
        if (total.getVisibility()==View.VISIBLE)
        {
            uso.setVisibility(View.VISIBLE);
            total.setVisibility(View.INVISIBLE);
            ( (LinearLayout)findViewById(R.id.doLayout)).removeAllViews();

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);

        if(null!=data){

            if(requestCode==1){
                //Do something
                String message=data.getStringExtra("MESSAGE_package_name");

               PackToADD= message;

            }
        }
    }
    String CriadordeHorario(int segundos)
    {
        int minutos=0,horas=0,segundos2 =0;
        if (segundos>=60)
        {
            minutos = segundos/60;
            segundos2= segundos%60;
            if (minutos>=60)
            {
                horas= minutos/60;
                minutos= minutos%60;
            }
        }
        else segundos2=segundos;
        if (((horas>0)? horas+"h ":"") + ((minutos>0)?minutos+"min ":"" )+ ((segundos2>0)?segundos2+"seg ":"") !="")
        return((horas>0)? horas+"h ":"") + ((minutos>0)?minutos+"min ":"" )+ ((segundos2>0)?segundos2+"seg ":"") ;
        else return "Sem uso";
    }
    void CreateWhatDo(LinearLayout layout,int totaltime)
    {
        if((layout).getChildCount() > 0)
            (layout).removeAllViews();
        int[] suffler = new int[toDoImages.size()];
        for (int i =0;i<suffler.length;i++)suffler[i]=i;
        shuffleArray(suffler);

        int howmuch =totaltime;

        if (totaltime/3600<2)
        {
            howmuch=0;
        }
        else if (totaltime/3600<=toDoImages.size())howmuch= totaltime/3600;
        else howmuch= toDoImages.size();
      //  findViewById(R.id.textView5).setVisibility(View.VISIBLE);

        for(int i=0;i<howmuch;i++){
            RelativeLayout temp = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.whatdo,null);
            ((ImageView) temp.findViewById(R.id.doImage)).setImageBitmap(BitmapFactory.decodeResource(getResources(),
                    toDoImages.get(suffler[i])));
            ( (TextView)temp.findViewById(R.id.whatis)).setText(toDoTitles.get(suffler[i]));
        layout.addView(temp);
    }
        if (howmuch==0)
        {
         //   findViewById(R.id.textView5).setVisibility(View.INVISIBLE);
            RelativeLayout temp = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.whatdo,null);
            ((ImageView) temp.findViewById(R.id.doImage)).setImageBitmap(BitmapFactory.decodeResource(getResources(),
                   R.drawable.trofeu));
            ( (TextView)temp.findViewById(R.id.whatis)).setText("Parabéns, você usa pouco as redes sociais");
            layout.addView(temp);
        }


    }
   int randomColor()
    {
        Random rnd = new Random();
        int color = Color.argb(255, 105, 141, 195);
        return  color;
    }

    static void shuffleArray(int[] ar)
    {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
    void addnewRelative(LinearLayout tolayout, Drawable resID, final String pkgname, int value)
    {

        RelativeLayout temp = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.tey,null);
        if (controlercor%2==0) {
            temp.setBackgroundColor(randomColor());
        }
        else temp.setBackgroundColor(Color.argb(255,255,255,255));
        for(int i =0;i<temp.getChildCount();i++)
        {
            if (temp.getChildAt(i) instanceof TextView)
            {
               if(( (TextView) temp.getChildAt(i)).getVisibility() == View.INVISIBLE
                       )
                   (
                               (TextView) temp.getChildAt(i)).setText(pkgname);
                else{
                   ( (TextView) temp.getChildAt(i)).setText(CriadordeHorario(value));
                   if (controlercor%2!=0) {
                       ( (TextView) temp.getChildAt(i)).setTextColor(randomColor());
                   }
                   else ( (TextView) temp.getChildAt(i)).setTextColor(Color.argb(255,255,255,255));

            }
            }
            else if (temp.getChildAt(i) instanceof ImageView && temp.getChildAt(i).getId()==R.id.img)
            {
                ((ImageView) temp.getChildAt(i)).setImageDrawable(resID);
                ((ImageView) temp.getChildAt(i)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = getPackageManager().getLaunchIntentForPackage(pkgname);
                        startActivity(i);
                    }
                });
            }
        }
        tolayout.addView(temp,0);

    }
    public boolean isPackageExisted(String targetPackage){
        PackageManager pm=getPackageManager();
        try {
            PackageInfo info=pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        if (splashtime>45)
        SplashScreen.setAlpha(lerp(SplashScreen.getAlpha(),0,0.2f));
        splashtime++;
        if (SplashScreen.getAlpha()<=0.1f)
        {
            father.setPadding(16,16,16,16);
            getSupportActionBar().show();
            barrinha.setMinimumHeight(70* getResources().getDisplayMetrics().heightPixels / 1280);
            barrinha.setMaxHeight( 70* getResources().getDisplayMetrics().heightPixels / 1280);
            SplashScreen.setVisibility(View.INVISIBLE);
        }
        if (mBound&&PackToADD!=null)
        {
            if (!checkIfisMonitoring(PackToADD,mService.AppsList)){
                Drawable icon = null;
                try {
                    icon = getPackageManager().getApplicationIcon(PackToADD);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                mService.AppsList.add(new apptocheck(PackToADD,0,"false","false",0));
                addnewRelative(ll,icon,PackToADD,0);
                PackToADD= null;
            }else {
                Toast.makeText(this,"Você já monitora esse App",Toast.LENGTH_LONG).show();
                PackToADD= null;
            }
        }
        if (mBound&&!done)
        {
           // if (AppsList!=null)  AppsList = ViciService.Sort(AppsList);
            mService.AppsList= AppsList;
            done = true;
        }
        if (mBound)
        {
         //   mService.AppsList= ViciService.Sort(mService.AppsList);
            WriteValues(ll, mService.AppsList);
        }
        h.postDelayed(this,30);
    }
    void ServiceStart()
    {
        if(!isMyServiceRunning(ViciService.class))        {

            startService(intent);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        }
        else {
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }
    void WriteValues(LinearLayout ll,List<apptocheck>Apps)
    {
        for(int i =0;i<ll.getChildCount();i++)
        {

            if (ll.getChildAt(i) instanceof RelativeLayout ) {

                TextView packname = null, usetime = null;
                RelativeLayout rl = (RelativeLayout) ll.getChildAt(i);
                if (rl.getId()!=R.id.ResetLayout){
                for (int z = 0; z < rl.getChildCount(); z++) {
                    if (rl.getChildAt(z) instanceof TextView && !((TextView) rl.getChildAt(z)).getText().equals("Resetar as estatística :")) {
                        if (rl.getChildAt(z) instanceof TextView) {
                            if ((rl.getChildAt(z)).getVisibility() == View.INVISIBLE) {
                                packname = (TextView) rl.getChildAt(z);
                            } else usetime = (TextView) rl.getChildAt(z);
                        }


                    if (packname != null && packname.getText().toString() == FindWithPack(Apps, packname.getText().toString()).packagename) {

                        usetime.setText(CriadordeHorario(FindWithPack(Apps, packname.getText().toString()).useTime));
                    }
                }
                }
                }

            }

        }
    }
    apptocheck FindWithPack(List<apptocheck> aps,String pack)
    {
        for (apptocheck apss:aps    ) {
            if (apss.packagename == pack)return  apss;

        }
        return  null;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {

        ViciService.LocalBinder binder = (ViciService.LocalBinder) service;
        mService = binder.getService();
        mBound = true;
        Log.d("bind service", "BINDDDDDDDDDDDD222222222222222222222222222");
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mBound = false;
    }
    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public static void doRestart(Context c) {
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                       debug( "Was not able to restart application, mStartActivity null");
                    }
                } else {
                     debug( "Was not able to restart application, PM null");
                }
            } else {
                 debug( "Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
             debug( "Was not able to restart application");
        }
    }

    float lerp(float v0, float v1, float t) {
        return (1 - t) * v0 + t * v1;
    }
}
