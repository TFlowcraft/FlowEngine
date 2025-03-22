/*
 * This file is generated by jOOQ.
 */
package com.database.entity.generated.tables.records;


import com.database.entity.generated.tables.PgpArmorHeaders;

import org.jooq.impl.TableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class PgpArmorHeadersRecord extends TableRecordImpl<PgpArmorHeadersRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.pgp_armor_headers.key</code>.
     */
    public PgpArmorHeadersRecord setKey(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.pgp_armor_headers.key</code>.
     */
    public String getKey() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.pgp_armor_headers.value</code>.
     */
    public PgpArmorHeadersRecord setValue(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.pgp_armor_headers.value</code>.
     */
    public String getValue() {
        return (String) get(1);
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PgpArmorHeadersRecord
     */
    public PgpArmorHeadersRecord() {
        super(PgpArmorHeaders.PGP_ARMOR_HEADERS);
    }

    /**
     * Create a detached, initialised PgpArmorHeadersRecord
     */
    public PgpArmorHeadersRecord(String key, String value) {
        super(PgpArmorHeaders.PGP_ARMOR_HEADERS);

        setKey(key);
        setValue(value);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised PgpArmorHeadersRecord
     */
    public PgpArmorHeadersRecord(com.database.entity.generated.tables.pojos.PgpArmorHeaders value) {
        super(PgpArmorHeaders.PGP_ARMOR_HEADERS);

        if (value != null) {
            setKey(value.getKey());
            setValue(value.getValue());
            resetChangedOnNotNull();
        }
    }
}
