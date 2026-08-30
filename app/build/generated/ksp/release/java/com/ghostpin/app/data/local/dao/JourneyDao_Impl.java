package com.ghostpin.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.ghostpin.app.data.local.entity.SavedJourneyEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class JourneyDao_Impl implements JourneyDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SavedJourneyEntity> __insertionAdapterOfSavedJourneyEntity;

  private final EntityDeletionOrUpdateAdapter<SavedJourneyEntity> __deletionAdapterOfSavedJourneyEntity;

  private final EntityDeletionOrUpdateAdapter<SavedJourneyEntity> __updateAdapterOfSavedJourneyEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteJourneyById;

  public JourneyDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSavedJourneyEntity = new EntityInsertionAdapter<SavedJourneyEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `saved_journeys` (`id`,`name`,`waypointsJson`,`travelSpeedKmh`,`loopRoute`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SavedJourneyEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getWaypointsJson());
        statement.bindDouble(4, entity.getTravelSpeedKmh());
        final int _tmp = entity.getLoopRoute() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfSavedJourneyEntity = new EntityDeletionOrUpdateAdapter<SavedJourneyEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `saved_journeys` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SavedJourneyEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfSavedJourneyEntity = new EntityDeletionOrUpdateAdapter<SavedJourneyEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `saved_journeys` SET `id` = ?,`name` = ?,`waypointsJson` = ?,`travelSpeedKmh` = ?,`loopRoute` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SavedJourneyEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getWaypointsJson());
        statement.bindDouble(4, entity.getTravelSpeedKmh());
        final int _tmp = entity.getLoopRoute() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getCreatedAt());
        statement.bindLong(7, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteJourneyById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM saved_journeys WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertJourney(final SavedJourneyEntity journey,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSavedJourneyEntity.insertAndReturnId(journey);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteJourney(final SavedJourneyEntity journey,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSavedJourneyEntity.handle(journey);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateJourney(final SavedJourneyEntity journey,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSavedJourneyEntity.handle(journey);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteJourneyById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteJourneyById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteJourneyById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SavedJourneyEntity>> getAllJourneys() {
    final String _sql = "SELECT * FROM saved_journeys ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"saved_journeys"}, new Callable<List<SavedJourneyEntity>>() {
      @Override
      @NonNull
      public List<SavedJourneyEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfWaypointsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "waypointsJson");
          final int _cursorIndexOfTravelSpeedKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "travelSpeedKmh");
          final int _cursorIndexOfLoopRoute = CursorUtil.getColumnIndexOrThrow(_cursor, "loopRoute");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<SavedJourneyEntity> _result = new ArrayList<SavedJourneyEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SavedJourneyEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpWaypointsJson;
            _tmpWaypointsJson = _cursor.getString(_cursorIndexOfWaypointsJson);
            final double _tmpTravelSpeedKmh;
            _tmpTravelSpeedKmh = _cursor.getDouble(_cursorIndexOfTravelSpeedKmh);
            final boolean _tmpLoopRoute;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfLoopRoute);
            _tmpLoopRoute = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new SavedJourneyEntity(_tmpId,_tmpName,_tmpWaypointsJson,_tmpTravelSpeedKmh,_tmpLoopRoute,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getJourneyById(final long id,
      final Continuation<? super SavedJourneyEntity> $completion) {
    final String _sql = "SELECT * FROM saved_journeys WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SavedJourneyEntity>() {
      @Override
      @Nullable
      public SavedJourneyEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfWaypointsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "waypointsJson");
          final int _cursorIndexOfTravelSpeedKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "travelSpeedKmh");
          final int _cursorIndexOfLoopRoute = CursorUtil.getColumnIndexOrThrow(_cursor, "loopRoute");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final SavedJourneyEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpWaypointsJson;
            _tmpWaypointsJson = _cursor.getString(_cursorIndexOfWaypointsJson);
            final double _tmpTravelSpeedKmh;
            _tmpTravelSpeedKmh = _cursor.getDouble(_cursorIndexOfTravelSpeedKmh);
            final boolean _tmpLoopRoute;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfLoopRoute);
            _tmpLoopRoute = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new SavedJourneyEntity(_tmpId,_tmpName,_tmpWaypointsJson,_tmpTravelSpeedKmh,_tmpLoopRoute,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
