-- pgcrypto для генерации UUID
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE process_info (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bpmn_process_id VARCHAR(255),
    processName VARCHAR(100),
    bpmn_file XML
);


-- process_instance
CREATE TABLE process_instance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_data JSONB,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    process_id UUID REFERENCES process_info(id) ON DELETE CASCADE
);

-- GIN индекс на business_data
CREATE INDEX idx_process_instance_business_data ON process_instance USING GIN (business_data);

-- instance_tasks
CREATE TABLE instance_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instance_id UUID REFERENCES process_instance(id) ON DELETE CASCADE,
    bpmn_element_id VARCHAR(255),
    status VARCHAR(50) CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    start_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    current_retries_amount INT
);

-- Индексы instance_tasks
CREATE INDEX idx_instance_tasks_instance_id ON instance_tasks (instance_id);
CREATE INDEX idx_instance_tasks_bpmn_element_id ON instance_tasks (bpmn_element_id);

-- instance_history
CREATE TABLE instance_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instance_id UUID REFERENCES process_instance(id) ON DELETE CASCADE,
    task_id UUID REFERENCES instance_tasks(id) ON DELETE CASCADE,
    task_status VARCHAR(50) CHECK (task_status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    error_stacktrace JSONB,
    timestamp TIMESTAMPTZ
);


-- Индексы instance_history
CREATE INDEX idx_instance_history_instance_id ON instance_history (instance_id);
CREATE INDEX idx_instance_history_task_id ON instance_history (task_id);

-- Функция триггера
CREATE OR REPLACE FUNCTION log_task_status_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        INSERT INTO instance_history (instance_id, task_id, task_status, timestamp)
        VALUES (NEW.instance_id, NEW.id, NEW.status, NOW());
END IF;

    -- Лог, если FAILED
    IF NEW.status = 'FAILED' THEN
        INSERT INTO instance_history (instance_id, task_id, task_status, error_stacktrace, timestamp)
        VALUES (NEW.instance_id, NEW.id, NEW.status, NEW.error_stacktrace, NOW());
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Функция вставки лога
CREATE OR REPLACE FUNCTION log_task_creation()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO instance_history (instance_id, task_id, task_status, timestamp)
    VALUES (NEW.instance_id, NEW.id, NEW.status, NOW());
RETURN NEW;
END;
$$  LANGUAGE  plpgsql;

-- Триггер логирования изменений статуса
CREATE TRIGGER trigger_log_task_status_change
    AFTER UPDATE ON instance_tasks
    FOR EACH ROW
    EXECUTE FUNCTION log_task_status_change();

-- Логирование при создании таски
CREATE TRIGGER trigger_log_task_creation
    AFTER INSERT ON instance_tasks
    FOR EACH ROW
    EXECUTE FUNCTION log_task_creation();