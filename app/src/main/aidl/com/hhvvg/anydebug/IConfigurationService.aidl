// IConfigurationService.aidl
package com.hhvvg.anydebug;

// Declare any non-default types here with import statements

interface IConfigurationService {
    boolean isEditEnabled();
    boolean isPersistentEnabled();
    void setEditEnabled(boolean enabled);
    void setPersistentEnabled(boolean enabled);
}
