package com.hhvvg.anydebug.persistent

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "table_view_rules", indices = [Index(unique = true, value = ["view_parent_id", "view_id", "view_rule_type"])])
data class ViewRule(
    @PrimaryKey(autoGenerate = true) val ruleId: Int = 0,
    @ColumnInfo(name = "view_class_name") val className: String,
    @ColumnInfo(name = "view_parent_id") val viewParentId: Int,
    @ColumnInfo(name = "view_id") val viewId: Int,
    @ColumnInfo(name = "view_rule_type") val ruleType: RuleType,
    @ColumnInfo(name = "view_rule") val viewRule: String,
    @ColumnInfo(name = "origin_view_content") val originViewContent: String? = null
)
