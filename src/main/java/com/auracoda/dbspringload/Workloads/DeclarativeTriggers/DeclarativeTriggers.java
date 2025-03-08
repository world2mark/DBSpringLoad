package com.auracoda.dbspringload.Workloads.DeclarativeTriggers;

import javax.sql.DataSource;

import com.auracoda.dbspringload.Workloads.BusinessLogicInterface;
import com.auracoda.dbspringload.Workloads.WorkloadService;

public class DeclarativeTriggers extends WorkloadService {

        public DeclarativeTriggers() {
                AddTableDefinition(
                                "create table audit_log_on_deletes (" +
                                                "  id UUID PRIMARY KEY DEFAULT uuid_generate_v4()," +
                                                "  table_name TEXT NOT NULL," +
                                                "  operation TEXT NOT NULL," +
                                                "  old_data JSONB," +
                                                "  new_data JSONB," +
                                                "  changed_at TIMESTAMP DEFAULT current_timestamp" +
                                                "  );",
                                "drop table audit_log_on_deletes");

                AddTableDefinition("CREATE FUNCTION audit_my_delete() RETURNS TRIGGER AS $$" +
                                "BEGIN" +
                                "  INSERT INTO audit_log_on_deletes (table_name, operation, old_data, new_data, changed_at)"
                                +
                                "  VALUES (TG_TABLE_NAME, TG_OP, row_to_json(OLD), row_to_json(NEW), current_timestamp);"
                                +
                                "  RETURN NULL;" +
                                "END;" +
                                "$$ LANGUAGE PLpgSQL;",
                                "drop function audit_my_delete");

                AddTableDefinition("create table my_ttl_table (" +
                                "  key string primary key," +
                                "  value string," +
                                "  expired_at timestamptz" +
                                ") with (ttl_expiration_expression = 'expired_at', ttl_job_cron = '*/2 * * * *');",
                                "drop table my_ttl_table");

                AddTableDefinition("alter table my_ttl_table configure zone using gc.ttlseconds = 300; -- every 5 min",
                                null);

                AddTableDefinition("CREATE TRIGGER audit_delete_trigger " +
                                "AFTER DELETE ON my_ttl_table " +
                                "FOR EACH ROW " +
                                "EXECUTE FUNCTION audit_my_delete()",
                                "drop trigger audit_delete_trigger on my_ttl_table");

        };

        @Override
        public String getDescription() {
                return "BUG: Unable to drop triggers within DBeaver, Java SpringBoot, and other JPA/ORM environments. <b>DROP TRIGGER</b> in particular does not have a legacy implementation, and so the result is an error. Bug has been identified and is being revied.  See <a target=\"#\" href=\"https://github.com/cockroachdb/cockroach/issues/141810\">github.com/cockroachdb/cockroach/issues/141810</a>.";
        }

        @Override
        public BusinessLogicInterface CreateBusinessLogicInstance(
                        long threadIndex,
                        DataSource myDataSource) throws Exception {
                return new BusinessLogic(myDataSource);
        }

}
