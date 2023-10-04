package Data;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.musicplayer.R;
import com.example.musicplayer.myproject.Activities.MainActivity;

import Services.WidgetService;

/**
 * Implementation of App Widget functionality.
 */
public class MusicAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
       // RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_app_widget);
        RemoteViews views = new RemoteViews(context.getPackageName() , R.layout.music_app_widget);

        // intent to start the app
        Intent startApp = new Intent(context , MainActivity.class);
        PendingIntent startAppIntent = PendingIntent.getActivity(context,0,startApp,0);
        views.setOnClickPendingIntent(R.id.widget_song_cover , startAppIntent);

        // intent to start playing music
        Intent startPlaying = new Intent(context , WidgetService.class);
        startPlaying.setAction(WidgetService.PLAY_PAUSE);
        PendingIntent startPlayingIntent = PendingIntent.getService(context , 0 , startPlaying ,PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_play_pause , startPlayingIntent);


        // intent to skip to next song
        Intent skipNext = new Intent(context , WidgetService.class);
        startPlaying.setAction(WidgetService.SKIP_NEXT);
        PendingIntent skipNextIntent = PendingIntent.getService(context , 0 , skipNext ,PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_play_pause , skipNextIntent);


        // intent to skip to previous song
        Intent skipPrevious = new Intent(context , WidgetService.class);
        startPlaying.setAction(WidgetService.SKIP_PREVIOUS);
        PendingIntent skipPreviousIntent = PendingIntent.getService(context , 0 , skipPrevious ,PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_play_pause , skipPreviousIntent);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);

        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}