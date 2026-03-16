--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5
-- Dumped by pg_dump version 17.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: audit_log; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.audit_log (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid,
    username character varying(50),
    ip_address character varying(45),
    user_agent character varying(255),
    action character varying(100) NOT NULL,
    resource character varying(100),
    resource_id character varying(100),
    old_value jsonb,
    new_value jsonb,
    channel character varying(20),
    http_method character varying(10),
    endpoint character varying(255),
    session_id uuid,
    correlation_id uuid,
    module character varying(50),
    status character varying(20) NOT NULL,
    failure_reason character varying(255),
    severity character varying(10) DEFAULT 'INFO'::character varying,
    metadata jsonb,
    created_at timestamp with time zone DEFAULT now()
);


ALTER TABLE public.audit_log OWNER TO postgres;

--
-- Name: TABLE audit_log; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.audit_log IS 'Immutable append-only log of every significant action in the system. Covers auth events, data changes, ATM transactions and system operations. No row may ever be updated or deleted.';


--
-- Name: COLUMN audit_log.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.id IS 'Unique identifier for the audit entry.';


--
-- Name: COLUMN audit_log.user_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.user_id IS 'ID of the user who performed the action. Nullable to support system and anonymous actions. No FK to avoid write failures if user is deleted.';


--
-- Name: COLUMN audit_log.username; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.username IS 'Snapshot of the username at the time of the action. Preserved even if the user is later deleted or anonymised.';


--
-- Name: COLUMN audit_log.ip_address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.ip_address IS 'IP address from which the action originated. Supports IPv4 and IPv6.';


--
-- Name: COLUMN audit_log.user_agent; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.user_agent IS 'Browser, ATM client or API agent string. Useful for device-level forensics.';


--
-- Name: COLUMN audit_log.action; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.action IS 'Uppercase action code describing what happened e.g. LOGIN, LOGOUT, TRANSFER_CREATE, ACCOUNT_FREEZE, PIN_CHANGE.';


--
-- Name: COLUMN audit_log.resource; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.resource IS 'Entity type that was acted upon e.g. USER, ACCOUNT, CARD, LOAN, SESSION.';


--
-- Name: COLUMN audit_log.resource_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.resource_id IS 'ID of the specific entity that was acted upon.';


--
-- Name: COLUMN audit_log.old_value; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.old_value IS 'JSON snapshot of the entity state before the change. Null for create or read actions.';


--
-- Name: COLUMN audit_log.new_value; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.new_value IS 'JSON snapshot of the entity state after the change. Null for delete or read actions.';


--
-- Name: COLUMN audit_log.channel; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.channel IS 'Access channel that originated the action. One of: WEB, ATM, API, MOBILE, SYSTEM.';


--
-- Name: COLUMN audit_log.http_method; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.http_method IS 'HTTP method of the originating request e.g. GET, POST, PUT, DELETE. Null for non-HTTP actions.';


--
-- Name: COLUMN audit_log.endpoint; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.endpoint IS 'API endpoint path that was called e.g. /api/v1/accounts/transfer. Null for non-HTTP actions.';


--
-- Name: COLUMN audit_log.session_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.session_id IS 'ID of the user session associated with this action. Nullable. No FK to avoid write failures on expired sessions.';


--
-- Name: COLUMN audit_log.correlation_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.correlation_id IS 'UUID that groups multiple audit entries belonging to the same logical request or workflow.';


--
-- Name: COLUMN audit_log.module; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.module IS 'Module that produced this audit entry e.g. AUTH, ACCOUNTS, ATM, LOANS.';


--
-- Name: COLUMN audit_log.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.status IS 'Outcome of the action. One of: SUCCESS, FAILURE, PARTIAL.';


--
-- Name: COLUMN audit_log.failure_reason; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.failure_reason IS 'Human-readable reason for failure. Populated only when status is FAILURE or PARTIAL.';


--
-- Name: COLUMN audit_log.severity; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.severity IS 'Severity level of the event. One of: INFO, WARN, CRITICAL. Used for alerting and filtering.';


--
-- Name: COLUMN audit_log.metadata; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.metadata IS 'Arbitrary JSON for any additional context that does not fit a dedicated column e.g. ATM terminal ID, geolocation, transaction reference.';


--
-- Name: COLUMN audit_log.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.audit_log.created_at IS 'Timestamp with timezone of when the event occurred. Always set by the database.';


--
-- Name: module_permissions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.module_permissions (
    module_id uuid NOT NULL,
    permission_id uuid NOT NULL
);


ALTER TABLE public.module_permissions OWNER TO postgres;

--
-- Name: TABLE module_permissions; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.module_permissions IS 'Maps permissions to the module they belong to. When a module is disabled all its linked permissions are effectively revoked.';


--
-- Name: COLUMN module_permissions.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.module_permissions.module_id IS 'Reference to the owning module.';


--
-- Name: COLUMN module_permissions.permission_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.module_permissions.permission_id IS 'Reference to the permission belonging to this module.';


--
-- Name: modules; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.modules (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    code character varying(50) NOT NULL,
    name character varying(100) NOT NULL,
    description character varying(255),
    is_enabled boolean DEFAULT true,
    version character varying(20),
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    created_by uuid DEFAULT '00000000-0000-0000-0000-000000000001'::uuid NOT NULL,
    updated_by uuid DEFAULT '00000000-0000-0000-0000-000000000001'::uuid NOT NULL
);


ALTER TABLE public.modules OWNER TO postgres;

--
-- Name: TABLE modules; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.modules IS 'Pluggable feature modules that can be enabled or disabled at runtime. Disabling a module blocks all access to its permissions regardless of role.';


--
-- Name: COLUMN modules.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.modules.id IS 'Unique identifier for the module.';


--
-- Name: COLUMN modules.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.modules.code IS 'Unique machine-readable module code e.g. LOANS, CARDS, ATM, FOREX.';


--
-- Name: COLUMN modules.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.modules.name IS 'Human-readable module name.';


--
-- Name: COLUMN modules.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.modules.description IS 'Description of what this module covers.';


--
-- Name: COLUMN modules.is_enabled; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.modules.is_enabled IS 'Master switch. When false all permissions belonging to this module are blocked system-wide.';


--
-- Name: COLUMN modules.version; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.modules.version IS 'Semantic version of the module e.g. 1.0.0. Useful for tracking module upgrades.';


--
-- Name: COLUMN modules.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.modules.created_at IS 'Timestamp when the module was registered.';


--
-- Name: COLUMN modules.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.modules.updated_at IS 'Timestamp when the module was last modified.';


--
-- Name: COLUMN modules.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.modules.created_by IS 'User ID who registered this module. Defaults to system user.';


--
-- Name: COLUMN modules.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.modules.updated_by IS 'User ID who last modified this module. Defaults to system user.';


--
-- Name: password_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.password_history (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    password_hash character varying(255) NOT NULL,
    created_at timestamp with time zone DEFAULT now(),
    created_by uuid DEFAULT '00000000-0000-0000-0000-000000000001'::uuid NOT NULL
);


ALTER TABLE public.password_history OWNER TO postgres;

--
-- Name: TABLE password_history; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.password_history IS 'Stores hashed previous passwords per user to enforce password reuse policies. Typically the last 5 to 12 passwords are retained.';


--
-- Name: COLUMN password_history.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.password_history.id IS 'Unique identifier for the password history entry.';


--
-- Name: COLUMN password_history.user_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.password_history.user_id IS 'Reference to the user this password history belongs to.';


--
-- Name: COLUMN password_history.password_hash; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.password_history.password_hash IS 'Hashed previous password. Used to prevent reuse during password change.';


--
-- Name: COLUMN password_history.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.password_history.created_at IS 'Timestamp when this password was set. Useful for enforcing age-based password expiry.';


--
-- Name: COLUMN password_history.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.password_history.created_by IS 'User ID who triggered the password change. Could be the user themselves or an admin. Defaults to system user.';


--
-- Name: permissions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.permissions (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    code character varying(100) NOT NULL,
    name character varying(100) NOT NULL,
    description character varying(255),
    created_at timestamp with time zone DEFAULT now(),
    created_by uuid DEFAULT '00000000-0000-0000-0000-000000000001'::uuid NOT NULL
);


ALTER TABLE public.permissions OWNER TO postgres;

--
-- Name: TABLE permissions; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.permissions IS 'Granular permission codes that control access to specific actions. Permissions are grouped into modules and assigned to roles.';


--
-- Name: COLUMN permissions.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.permissions.id IS 'Unique identifier for the permission.';


--
-- Name: COLUMN permissions.code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.permissions.code IS 'Machine-readable permission code e.g. ACCOUNT_VIEW, TRANSFER_CREATE, CARD_BLOCK.';


--
-- Name: COLUMN permissions.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.permissions.name IS 'Human-readable permission label.';


--
-- Name: COLUMN permissions.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.permissions.description IS 'Explanation of what this permission grants access to.';


--
-- Name: COLUMN permissions.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.permissions.created_at IS 'Timestamp when the permission was created.';


--
-- Name: COLUMN permissions.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.permissions.created_by IS 'User ID who created this permission. Defaults to system user.';


--
-- Name: role_permissions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.role_permissions (
    role_id uuid NOT NULL,
    permission_id uuid NOT NULL,
    assigned_at timestamp with time zone DEFAULT now(),
    assigned_by uuid DEFAULT '00000000-0000-0000-0000-000000000001'::uuid NOT NULL
);


ALTER TABLE public.role_permissions OWNER TO postgres;

--
-- Name: TABLE role_permissions; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.role_permissions IS 'Many-to-many mapping between roles and permissions. Defines what a role is allowed to do.';


--
-- Name: COLUMN role_permissions.role_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.role_permissions.role_id IS 'Reference to the role.';


--
-- Name: COLUMN role_permissions.permission_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.role_permissions.permission_id IS 'Reference to the permission being granted to the role.';


--
-- Name: COLUMN role_permissions.assigned_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.role_permissions.assigned_at IS 'Timestamp when the permission was added to the role.';


--
-- Name: COLUMN role_permissions.assigned_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.role_permissions.assigned_by IS 'User ID who granted the permission to the role. Defaults to system user.';


--
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(255),
    is_system boolean DEFAULT false,
    is_active boolean DEFAULT true,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    created_by uuid DEFAULT '00000000-0000-0000-0000-000000000001'::uuid NOT NULL,
    updated_by uuid DEFAULT '00000000-0000-0000-0000-000000000001'::uuid NOT NULL
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- Name: TABLE roles; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.roles IS 'Defines named roles that group permissions together. Roles are assigned to users. System roles cannot be deleted.';


--
-- Name: COLUMN roles.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.roles.id IS 'Unique identifier for the role.';


--
-- Name: COLUMN roles.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.roles.name IS 'Unique role name e.g. ADMIN, TELLER, CUSTOMER, AUDITOR, ATM.';


--
-- Name: COLUMN roles.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.roles.description IS 'Human-readable explanation of what this role allows.';


--
-- Name: COLUMN roles.is_system; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.roles.is_system IS 'When true this role is built-in and cannot be deleted or renamed.';


--
-- Name: COLUMN roles.is_active; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.roles.is_active IS 'When false this role cannot be assigned to new users.';


--
-- Name: COLUMN roles.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.roles.created_at IS 'Timestamp when the role was created.';


--
-- Name: COLUMN roles.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.roles.updated_at IS 'Timestamp when the role was last modified.';


--
-- Name: COLUMN roles.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.roles.created_by IS 'User ID who created this role. Defaults to system user.';


--
-- Name: COLUMN roles.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.roles.updated_by IS 'User ID who last modified this role. Defaults to system user.';


--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_roles (
    user_id uuid NOT NULL,
    role_id uuid NOT NULL,
    assigned_at timestamp with time zone DEFAULT now(),
    assigned_by uuid DEFAULT '00000000-0000-0000-0000-000000000001'::uuid NOT NULL
);


ALTER TABLE public.user_roles OWNER TO postgres;

--
-- Name: TABLE user_roles; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.user_roles IS 'Many-to-many mapping between users and roles. A user can hold multiple roles.';


--
-- Name: COLUMN user_roles.user_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_roles.user_id IS 'Reference to the user being assigned the role.';


--
-- Name: COLUMN user_roles.role_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_roles.role_id IS 'Reference to the role being assigned.';


--
-- Name: COLUMN user_roles.assigned_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_roles.assigned_at IS 'Timestamp when the role was assigned to the user.';


--
-- Name: COLUMN user_roles.assigned_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_roles.assigned_by IS 'User ID who performed the role assignment. Defaults to system user.';


--
-- Name: user_sessions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_sessions (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    refresh_token character varying(512) NOT NULL,
    device_info character varying(255),
    ip_address character varying(45),
    is_active boolean DEFAULT true,
    expires_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone DEFAULT now(),
    revoked_at timestamp with time zone,
    revoke_reason character varying(100)
);


ALTER TABLE public.user_sessions OWNER TO postgres;

--
-- Name: TABLE user_sessions; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.user_sessions IS 'Tracks active and historical user sessions. Each login issues a new session with a refresh token. Used for JWT refresh and forced logout.';


--
-- Name: COLUMN user_sessions.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_sessions.id IS 'Unique session identifier.';


--
-- Name: COLUMN user_sessions.user_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_sessions.user_id IS 'Reference to the authenticated user.';


--
-- Name: COLUMN user_sessions.refresh_token; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_sessions.refresh_token IS 'Hashed refresh token issued at login. Used to obtain new access tokens.';


--
-- Name: COLUMN user_sessions.device_info; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_sessions.device_info IS 'Device or client description e.g. browser name, ATM terminal ID, mobile OS.';


--
-- Name: COLUMN user_sessions.ip_address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_sessions.ip_address IS 'IP address from which the session was initiated.';


--
-- Name: COLUMN user_sessions.is_active; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_sessions.is_active IS 'When false the session is no longer valid. Checked on every token refresh.';


--
-- Name: COLUMN user_sessions.expires_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_sessions.expires_at IS 'Absolute expiry time of the refresh token. Sessions past this time are considered expired.';


--
-- Name: COLUMN user_sessions.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_sessions.created_at IS 'Timestamp when the session was created i.e. when the user logged in.';


--
-- Name: COLUMN user_sessions.revoked_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_sessions.revoked_at IS 'Timestamp when the session was explicitly revoked. Null if still active or naturally expired.';


--
-- Name: COLUMN user_sessions.revoke_reason; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_sessions.revoke_reason IS 'Reason for revocation e.g. LOGOUT, EXPIRED, ADMIN_REVOKE, PASSWORD_CHANGE.';


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    username character varying(50) NOT NULL,
    email character varying(100) NOT NULL,
    password_hash character varying(255) NOT NULL,
    first_name character varying(50),
    last_name character varying(50),
    phone_number character varying(20),
    is_active boolean DEFAULT true,
    is_locked boolean DEFAULT false,
    is_mfa_enabled boolean DEFAULT false,
    mfa_secret character varying(255),
    failed_login_attempts integer DEFAULT 0,
    last_login_at timestamp with time zone,
    last_login_ip character varying(45),
    locked_at timestamp with time zone,
    lock_reason character varying(255),
    password_changed_at timestamp with time zone DEFAULT now(),
    must_change_password boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    created_by uuid DEFAULT '00000000-0000-0000-0000-000000000001'::uuid NOT NULL,
    updated_by uuid DEFAULT '00000000-0000-0000-0000-000000000001'::uuid NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: TABLE users; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.users IS 'Core user accounts for all system actors — staff, customers, ATM service accounts and the reserved system user.';


--
-- Name: COLUMN users.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.id IS 'Unique identifier for the user. Generated automatically as a UUID.';


--
-- Name: COLUMN users.username; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.username IS 'Unique login handle. Used for authentication.';


--
-- Name: COLUMN users.email; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.email IS 'Unique email address. Used for notifications and account recovery.';


--
-- Name: COLUMN users.password_hash; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.password_hash IS 'Bcrypt/Argon2 hashed password. Never store plaintext.';


--
-- Name: COLUMN users.first_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.first_name IS 'User first name.';


--
-- Name: COLUMN users.last_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.last_name IS 'User last name.';


--
-- Name: COLUMN users.phone_number; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.phone_number IS 'Contact phone number. Used for MFA SMS and notifications.';


--
-- Name: COLUMN users.is_active; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.is_active IS 'When false the account is disabled and cannot authenticate.';


--
-- Name: COLUMN users.is_locked; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.is_locked IS 'When true the account is temporarily locked. See locked_at and lock_reason.';


--
-- Name: COLUMN users.is_mfa_enabled; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.is_mfa_enabled IS 'Whether TOTP-based multi-factor authentication is active for this user.';


--
-- Name: COLUMN users.mfa_secret; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.mfa_secret IS 'Encrypted TOTP secret key. Used to generate and verify MFA codes.';


--
-- Name: COLUMN users.failed_login_attempts; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.failed_login_attempts IS 'Rolling count of consecutive failed logins. Resets on successful login. Triggers lock after threshold.';


--
-- Name: COLUMN users.last_login_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.last_login_at IS 'Timestamp of the most recent successful login.';


--
-- Name: COLUMN users.last_login_ip; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.last_login_ip IS 'IP address of the most recent successful login. Used for anomaly detection.';


--
-- Name: COLUMN users.locked_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.locked_at IS 'Timestamp of when the account was locked. Null if not locked.';


--
-- Name: COLUMN users.lock_reason; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.lock_reason IS 'Human-readable reason the account was locked e.g. TOO_MANY_ATTEMPTS, ADMIN_ACTION.';


--
-- Name: COLUMN users.password_changed_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.password_changed_at IS 'Timestamp of the last password change. Used to enforce password expiry policies.';


--
-- Name: COLUMN users.must_change_password; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.must_change_password IS 'Forces the user to change their password on next login. Set on account creation or admin reset.';


--
-- Name: COLUMN users.created_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.created_at IS 'Timestamp when the record was created.';


--
-- Name: COLUMN users.updated_at; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.updated_at IS 'Timestamp when the record was last updated.';


--
-- Name: COLUMN users.created_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.created_by IS 'User ID who created this record. Defaults to the system user 00000000-0000-0000-0000-000000000001 for SQL-level inserts.';


--
-- Name: COLUMN users.updated_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.updated_by IS 'User ID who last updated this record. Defaults to the system user 00000000-0000-0000-0000-000000000001 for SQL-level updates.';


--
-- Data for Name: audit_log; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.audit_log (id, user_id, username, ip_address, user_agent, action, resource, resource_id, old_value, new_value, channel, http_method, endpoint, session_id, correlation_id, module, status, failure_reason, severity, metadata, created_at) FROM stdin;
bbf93469-2c72-4e1e-9a25-4979ebc0e292	00000000-0000-0000-0000-000000000001	system	\N	\N	USER_CREATED	USER	120961de-b2d4-45fe-a224-5c59d475a272	\N	{"username": "admin"}	SYSTEM	\N	\N	\N	\N	AUTH	SUCCESS	\N	INFO	\N	2026-03-13 12:18:53.340824+12
\.


--
-- Data for Name: module_permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.module_permissions (module_id, permission_id) FROM stdin;
\.


--
-- Data for Name: modules; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.modules (id, code, name, description, is_enabled, version, created_at, updated_at, created_by, updated_by) FROM stdin;
1cb20633-50a5-4064-a75b-d1721e6a06e0	AUTH	Authentication & Authorization	\N	t	\N	2026-03-11 11:37:47.02265+12	2026-03-11 11:37:47.02265+12	00000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000001
85c46bb5-ec0a-416e-b0dd-61d92d2b8b11	ACCOUNTS	Account Management	\N	f	\N	2026-03-11 11:37:47.02265+12	2026-03-11 11:37:47.02265+12	00000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000001
5c21fa6e-567a-40d9-99da-e532a97b69c1	TRANSACTIONS	Transactions	\N	f	\N	2026-03-11 11:37:47.02265+12	2026-03-11 11:37:47.02265+12	00000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000001
49841bc1-9edc-4e4f-9d81-e8f54b95ad60	CARDS	Card Management	\N	f	\N	2026-03-11 11:37:47.02265+12	2026-03-11 11:37:47.02265+12	00000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000001
32c7166c-1175-4f94-8d7f-b8e552ff3465	LOANS	Loan Management	\N	f	\N	2026-03-11 11:37:47.02265+12	2026-03-11 11:37:47.02265+12	00000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000001
e92d99c6-2156-46e9-b222-f995b92f8209	ATM	ATM Gateway	\N	f	\N	2026-03-11 11:37:47.02265+12	2026-03-11 11:37:47.02265+12	00000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000001
bbd15abd-8bd4-4c54-aa39-80b58b4b17da	AUDIT	Audit & Reporting	\N	f	\N	2026-03-11 11:37:47.02265+12	2026-03-11 11:37:47.02265+12	00000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000001
c9f43096-9617-4b97-8302-cb7a84a8bab1	FOREX	Foreign Exchange	\N	f	\N	2026-03-11 11:37:47.02265+12	2026-03-11 11:37:47.02265+12	00000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000001
b9fcc27d-4ec9-42b7-a339-d1829c9ae710	HSM	HSM / Cryptographic Services	\N	f	\N	2026-03-11 11:37:47.02265+12	2026-03-11 11:37:47.02265+12	00000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000001
\.


--
-- Data for Name: password_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.password_history (id, user_id, password_hash, created_at, created_by) FROM stdin;
\.


--
-- Data for Name: permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.permissions (id, code, name, description, created_at, created_by) FROM stdin;
\.


--
-- Data for Name: role_permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.role_permissions (role_id, permission_id, assigned_at, assigned_by) FROM stdin;
\.


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.roles (id, name, description, is_system, is_active, created_at, updated_at, created_by, updated_by) FROM stdin;
94e27b7a-c4a1-4bab-94dc-e5aafd70e5be	ADMIN	Full system access	t	t	2026-03-11 11:37:15.667851+12	2026-03-11 11:37:15.667851+12	00000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000001
cc1a8aa8-154b-4ad1-b9f5-0819ceacd221	TELLER	Branch teller operations	t	t	2026-03-11 11:37:15.667851+12	2026-03-11 11:37:15.667851+12	00000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000001
3ca1ca2d-88a6-46ac-bbd3-1f0ba2c8097c	CUSTOMER	Customer self-service	t	t	2026-03-11 11:37:15.667851+12	2026-03-11 11:37:15.667851+12	00000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000001
c1d0d364-2f6c-4b8b-8c62-4da9236400f5	AUDITOR	Read-only audit access	t	t	2026-03-11 11:37:15.667851+12	2026-03-11 11:37:15.667851+12	00000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000001
2485a716-5f53-4604-aa12-882f653bcc8a	ATM_1	ATM_1 machine service account	t	t	2026-03-11 11:37:15.667851+12	2026-03-11 11:37:15.667851+12	00000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000001
\.


--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_roles (user_id, role_id, assigned_at, assigned_by) FROM stdin;
\.


--
-- Data for Name: user_sessions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_sessions (id, user_id, refresh_token, device_info, ip_address, is_active, expires_at, created_at, revoked_at, revoke_reason) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, username, email, password_hash, first_name, last_name, phone_number, is_active, is_locked, is_mfa_enabled, mfa_secret, failed_login_attempts, last_login_at, last_login_ip, locked_at, lock_reason, password_changed_at, must_change_password, created_at, updated_at, created_by, updated_by) FROM stdin;
00000000-0000-0000-0000-000000000001	system	system@oscbs.internal	N/A	System	\N	\N	f	t	f	\N	0	\N	\N	\N	\N	2026-03-11 11:35:27.874445+12	f	2026-03-11 11:35:27.874445+12	2026-03-11 11:35:27.874445+12	00000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000001
120961de-b2d4-45fe-a224-5c59d475a272	admin	admin@oscbs.internal	$2a$10$4so5WPZlkmctzP/i677.Cej/kPen0FXgmtR/fedJ0uBV2kl0.RI7m	Admin	User	\N	t	f	f	\N	0	\N	\N	\N	\N	2026-03-13 12:18:53.23593+12	t	2026-03-13 12:18:53.32391+12	2026-03-13 12:18:53.32391+12	00000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000001
\.


--
-- Name: audit_log audit_log_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.audit_log
    ADD CONSTRAINT audit_log_pkey PRIMARY KEY (id);


--
-- Name: module_permissions module_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.module_permissions
    ADD CONSTRAINT module_permissions_pkey PRIMARY KEY (module_id, permission_id);


--
-- Name: modules modules_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.modules
    ADD CONSTRAINT modules_code_key UNIQUE (code);


--
-- Name: modules modules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.modules
    ADD CONSTRAINT modules_pkey PRIMARY KEY (id);


--
-- Name: password_history password_history_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_history
    ADD CONSTRAINT password_history_pkey PRIMARY KEY (id);


--
-- Name: permissions permissions_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_code_key UNIQUE (code);


--
-- Name: permissions permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_pkey PRIMARY KEY (id);


--
-- Name: role_permissions role_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_pkey PRIMARY KEY (role_id, permission_id);


--
-- Name: roles roles_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_name_key UNIQUE (name);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);


--
-- Name: user_sessions user_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_sessions
    ADD CONSTRAINT user_sessions_pkey PRIMARY KEY (id);


--
-- Name: user_sessions user_sessions_refresh_token_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_sessions
    ADD CONSTRAINT user_sessions_refresh_token_key UNIQUE (refresh_token);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: idx_audit_action; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_audit_action ON public.audit_log USING btree (action);


--
-- Name: idx_audit_correlation; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_audit_correlation ON public.audit_log USING btree (correlation_id);


--
-- Name: idx_audit_created_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_audit_created_at ON public.audit_log USING btree (created_at DESC);


--
-- Name: idx_audit_module; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_audit_module ON public.audit_log USING btree (module);


--
-- Name: idx_audit_resource; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_audit_resource ON public.audit_log USING btree (resource, resource_id);


--
-- Name: idx_audit_session_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_audit_session_id ON public.audit_log USING btree (session_id);


--
-- Name: idx_audit_severity; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_audit_severity ON public.audit_log USING btree (severity);


--
-- Name: idx_audit_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_audit_user_id ON public.audit_log USING btree (user_id);


--
-- Name: idx_permissions_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_permissions_code ON public.permissions USING btree (code);


--
-- Name: idx_pwd_history_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_pwd_history_user ON public.password_history USING btree (user_id);


--
-- Name: idx_roles_name; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_roles_name ON public.roles USING btree (name);


--
-- Name: idx_sessions_active; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sessions_active ON public.user_sessions USING btree (is_active, expires_at);


--
-- Name: idx_sessions_token; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sessions_token ON public.user_sessions USING btree (refresh_token);


--
-- Name: idx_sessions_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sessions_user_id ON public.user_sessions USING btree (user_id);


--
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_email ON public.users USING btree (email);


--
-- Name: idx_users_is_active; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_is_active ON public.users USING btree (is_active);


--
-- Name: idx_users_username; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_username ON public.users USING btree (username);


--
-- Name: users fk_users_created_by; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: users fk_users_updated_by; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk_users_updated_by FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: module_permissions module_permissions_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.module_permissions
    ADD CONSTRAINT module_permissions_module_id_fkey FOREIGN KEY (module_id) REFERENCES public.modules(id) ON DELETE CASCADE;


--
-- Name: module_permissions module_permissions_permission_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.module_permissions
    ADD CONSTRAINT module_permissions_permission_id_fkey FOREIGN KEY (permission_id) REFERENCES public.permissions(id) ON DELETE CASCADE;


--
-- Name: modules modules_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.modules
    ADD CONSTRAINT modules_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: modules modules_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.modules
    ADD CONSTRAINT modules_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: password_history password_history_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_history
    ADD CONSTRAINT password_history_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: password_history password_history_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_history
    ADD CONSTRAINT password_history_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: permissions permissions_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: role_permissions role_permissions_assigned_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_assigned_by_fkey FOREIGN KEY (assigned_by) REFERENCES public.users(id);


--
-- Name: role_permissions role_permissions_permission_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_permission_id_fkey FOREIGN KEY (permission_id) REFERENCES public.permissions(id) ON DELETE CASCADE;


--
-- Name: role_permissions role_permissions_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id) ON DELETE CASCADE;


--
-- Name: roles roles_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: roles roles_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: user_roles user_roles_assigned_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_assigned_by_fkey FOREIGN KEY (assigned_by) REFERENCES public.users(id);


--
-- Name: user_roles user_roles_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id) ON DELETE CASCADE;


--
-- Name: user_roles user_roles_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: user_sessions user_sessions_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_sessions
    ADD CONSTRAINT user_sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

