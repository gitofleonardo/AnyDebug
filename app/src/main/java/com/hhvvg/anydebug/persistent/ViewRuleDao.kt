package com.hhvvg.anydebug.persistent

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ViewRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg rules: ViewRule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(rules: List<ViewRule>)

    @Delete
    fun deleteAll(vararg rules: ViewRule)

    @Delete
    fun deleteAll(rules: List<ViewRule>)

    @Query("SELECT * FROM table_view_rules")
    fun queryAllRules(): Flow<List<ViewRule>>

    @Query("SELECT * FROM table_view_rules WHERE view_id=:viewId and view_parent_id=:parentId and view_rule_type=:ruleType")
    fun findByIdAndParentIdAndType(viewId: Int, parentId: Int, ruleType: RuleType): ViewRule?
}