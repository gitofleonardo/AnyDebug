package com.hhvvg.anydebug.persistent

import androidx.room.TypeConverter

class RuleTypeConverter {
    @TypeConverter
    fun fromRuleTypeToInt(type: RuleType): Int {
        return type.value
    }

    @TypeConverter
    fun fromIntToRuleType(type: Int): RuleType {
        return when(type) {
            1 -> RuleType.Visibility
            2 -> RuleType.Text
            3 -> RuleType.TextSize
            4 -> RuleType.TextMaxLine
            else -> RuleType.None
        }
    }
}