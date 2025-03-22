/*
 * This file is generated by jOOQ.
 */
package com.database.entity.generated;


import com.database.entity.generated.tables.InstanceHistory;
import com.database.entity.generated.tables.InstanceTasks;
import com.database.entity.generated.tables.ProcessInstance;
import com.database.entity.generated.tables.records.InstanceHistoryRecord;
import com.database.entity.generated.tables.records.InstanceTasksRecord;
import com.database.entity.generated.tables.records.ProcessInstanceRecord;

import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in
 * public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<InstanceHistoryRecord> INSTANCE_HISTORY_PKEY = Internal.createUniqueKey(InstanceHistory.INSTANCE_HISTORY, DSL.name("instance_history_pkey"), new TableField[] { InstanceHistory.INSTANCE_HISTORY.ID }, true);
    public static final UniqueKey<InstanceTasksRecord> INSTANCE_TASKS_PKEY = Internal.createUniqueKey(InstanceTasks.INSTANCE_TASKS, DSL.name("instance_tasks_pkey"), new TableField[] { InstanceTasks.INSTANCE_TASKS.ID }, true);
    public static final UniqueKey<ProcessInstanceRecord> PROCESS_INSTANCE_PKEY = Internal.createUniqueKey(ProcessInstance.PROCESS_INSTANCE, DSL.name("process_instance_pkey"), new TableField[] { ProcessInstance.PROCESS_INSTANCE.ID }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<InstanceHistoryRecord, ProcessInstanceRecord> INSTANCE_HISTORY__INSTANCE_HISTORY_INSTANCE_ID_FKEY = Internal.createForeignKey(InstanceHistory.INSTANCE_HISTORY, DSL.name("instance_history_instance_id_fkey"), new TableField[] { InstanceHistory.INSTANCE_HISTORY.INSTANCE_ID }, Keys.PROCESS_INSTANCE_PKEY, new TableField[] { ProcessInstance.PROCESS_INSTANCE.ID }, true);
    public static final ForeignKey<InstanceHistoryRecord, InstanceTasksRecord> INSTANCE_HISTORY__INSTANCE_HISTORY_TASK_ID_FKEY = Internal.createForeignKey(InstanceHistory.INSTANCE_HISTORY, DSL.name("instance_history_task_id_fkey"), new TableField[] { InstanceHistory.INSTANCE_HISTORY.TASK_ID }, Keys.INSTANCE_TASKS_PKEY, new TableField[] { InstanceTasks.INSTANCE_TASKS.ID }, true);
    public static final ForeignKey<InstanceTasksRecord, ProcessInstanceRecord> INSTANCE_TASKS__INSTANCE_TASKS_INSTANCE_ID_FKEY = Internal.createForeignKey(InstanceTasks.INSTANCE_TASKS, DSL.name("instance_tasks_instance_id_fkey"), new TableField[] { InstanceTasks.INSTANCE_TASKS.INSTANCE_ID }, Keys.PROCESS_INSTANCE_PKEY, new TableField[] { ProcessInstance.PROCESS_INSTANCE.ID }, true);
}
