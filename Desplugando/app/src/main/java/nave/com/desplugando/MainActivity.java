package nave.com.desplugando;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Runnable,ServiceConnection
{
    ViciService mService;
    final ServiceConnection mConnection = this;
    Intent intent;
    boolean mBound = false;
    LinearLayout ll ;
    Handler h ;
    TextView fb,wpp,insta,twitter;
    Button usobt,voltar;
    RelativeLayout inicial,uso,modelo;
    List <apptocheck> AppsList;
    boolean  done;
    String[] InitialApps = new String[] {"com.facebook.katana","com.whatsapp","com.twitter.android","com.instagram.android"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(this,ViciService.class);
        ServiceStart();
        h= new Handler();
        h.post(this);
        done = false;
        ll = (LinearLayout) findViewById(R.id.ll);
        SharedPreferences sp = getSharedPreferences("prefs", Activity.MODE_PRIVATE);

        String applist =sp.getString("apps",null);
        if (applist==null)
        {
        AppsList = new ArrayList<>() ;
        for (int i=0;i<InitialApps.length;i++)
        {
            if (isPackageExisted(InitialApps[i]))
            {
                Drawable icon = null;
                try {
                    icon = getPackageManager().getApplicationIcon(InitialApps[i]);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                AppsList.add(new apptocheck(InitialApps[i],0,icon));
                addnewRelative(ll,icon,InitialApps[i]);
            }
        }

        if (mBound)
        {   mService.AppsList= AppsList;
            debug("manda p la ");
        }
        }else
        {

        }

    }
    void debug(String s)
    {
        Log.d("vicio", s);
    }

    @Override
    protected void onStart() {

      /*  fb= (TextView)findViewById(R.id.fb);
        wpp= (TextView)findViewById(R.id.wpp);
        insta =(TextView)findViewById(R.id.insta);
        twitter= (TextView)findViewById(R.id.tt);*/


        inicial =(RelativeLayout)findViewById(R.id.InicialLayout);
        uso = (RelativeLayout)findViewById(R.id.UsoLayout);
        usobt= (Button)findViewById(R.id.usoBt);
        inicial.setVisibility(View.VISIBLE);
        uso.setVisibility(View.INVISIBLE);
        usobt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inicial.setVisibility(View.INVISIBLE);
                uso.setVisibility(View.VISIBLE);
            }
        });
        voltar = (Button)findViewById(R.id.voltarBt);
        voltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inicial.setVisibility(View.VISIBLE);
                uso.setVisibility(View.INVISIBLE);
            }
        });

        super.onStart();
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

        return  horas+"h "+minutos+"min "+segundos2+"seg ";
    }
    void addnewRelative(LinearLayout tolayout,Drawable resID,String pkgname)
    {

        RelativeLayout temp = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.tey,null);
        for(int i =0;i<temp.getChildCount();i++)
        {
            if (temp.getChildAt(i) instanceof TextView)
            {
               if(( (TextView) temp.getChildAt(i)).getVisibility() == View.INVISIBLE
                       )
                   (
                               (TextView) temp.getChildAt(i)).setText(pkgname);

            }
            else if (temp.getChildAt(i) instanceof ImageView)
            {
                ((ImageView) temp.getChildAt(i)).setImageDrawable(resID);
            }
        }
        tolayout.addView(temp,tolayout.getChildCount()-1);

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

        if (mBound&&!done)
        {
            mService.AppsList= AppsList;
            done = true;
        }
        if (mBound)
        {
            WriteValues(ll, mService.AppsList);
        }
        h.postDelayed(this,1000);
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

            if (ll.getChildAt(i) instanceof RelativeLayout)
            {

                TextView packname= null, usetime=null;
                RelativeLayout rl =(RelativeLayout) ll.getChildAt(i);
                for (int z =0;z<rl.getChildCount();z++)
                {
                if (rl.getChildAt(z) instanceof TextView) {
                    if (( rl.getChildAt(z)).getVisibility() == View.INVISIBLE) {
                        packname = (TextView) rl.getChildAt(z);
                    } else usetime = (TextView) rl.getChildAt(z);
                }
              }

              if (packname.getText().toString()== FindWithPack(Apps,packname.getText().toString()).packagename)
              {

                  usetime.setText(CriadordeHorario( FindWithPack(Apps,packname.getText().toString()).useTime));
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

}
