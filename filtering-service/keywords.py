"""
Keyword Ontology — exact technology targets per user specification.
Each key is the canonical product name.
Each value is a list of keyword variants for matching.
"""

KEYWORD_ONTOLOGY = {
    "Ubuntu": [
        "ubuntu", "ubuntu linux", "ubuntu server", "ubuntu desktop",
        "canonical ubuntu", "focal fossa", "jammy jellyfish", "noble numbat"
    ],
    "Fortinet": [
        "fortinet", "fortios", "fortigate", "fortiweb", "fortimanager",
        "fortianalyzer", "fortisandbox", "forticlient", "fortiadc", "fortimail"
    ],
    "Microsoft Edge": [
        "microsoft edge", "edge browser", "edge chromium", "msedge"
    ],
    "Mozilla Firefox": [
        "mozilla firefox", "firefox", "mozilla", "firefox esr",
        "firefox quantum", "gecko"
    ],
    "Google Chrome": [
        "google chrome", "chrome browser", "chromium", "chrome os", "chromeos"
    ],
    "Windows": [
        "windows 10", "windows 11", "windows 7", "windows 8", "windows xp",
        "microsoft windows", "win32", "win64", "windows kernel",
        "windows defender", "windows update"
    ],
    "ALMA": [
        "almalinux", "alma linux", "alma os"
    ],
    "SQL Server": [
        "microsoft sql server", "sql server", "mssql", "sqlserver",
        "sql server 2019", "sql server 2022", "sql server 2017", "t-sql"
    ],
    "Oracle Database": [
        "oracle database", "oracle db", "oracle rdbms", "oracle 19c",
        "oracle 21c", "oracle 12c", "oracle 11g", "pl/sql", "oracle weblogic"
    ],
    "Android": [
        "android", "android os", "aosp", "android studio",
        "google android", "android kernel", "android vulnerability"
    ],
    "iOS": [
        "ios", "iphone os", "apple ios", "iphone", "ipad",
        "ipados", "watchos", "tvos"
    ],
    "Sage": [
        "sage software", "sage erp", "sage 100", "sage 300",
        "sage x3", "sage accounting"
    ],
    "Jira": [
        "jira", "atlassian jira", "jira software", "jira service management",
        "atlassian", "jira cloud", "jira server"
    ],
    "GLPI": [
        "glpi", "gestionnaire libre de parc informatique",
        "glpi itsm", "glpi helpdesk"
    ],
    "Microsoft Dynamics 365": [
        "dynamics 365", "microsoft dynamics", "dynamics crm",
        "dynamics erp", "d365", "dynamics nav", "dynamics ax"
    ],
    "Microsoft Office": [
        "microsoft office", "office 365", "ms office", "microsoft 365",
        "word", "excel", "powerpoint", "outlook", "onenote",
        "office suite", "microsoft word", "microsoft excel",
        "microsoft powerpoint", "microsoft outlook", "teams", "microsoft teams",
        "sharepoint", "onedrive"
    ],
    "Google Workspace": [
        "google workspace", "g suite", "google docs", "google sheets",
        "google slides", "google drive", "gmail", "google meet",
        "google admin", "google calendar"
    ],
    "WhatsApp": [
        "whatsapp", "whatsapp business", "whatsapp web",
        "whatsapp desktop", "meta whatsapp"
    ],
    "Shopify": [
        "shopify", "shopify plus", "shopify payments",
        "shopify admin", "shopify liquid"
    ],
    "Keepass2": [
        "keepass", "keepass2", "keepassxc", "keepassdx",
        "keepass password", "keepass database"
    ],
    "OpenAI": [
        "openai", "chatgpt", "gpt-4", "gpt-3", "openai api",
        "dall-e", "openai platform", "openai plugin"
    ],
    "Microsoft AX 2012": [
        "microsoft ax 2012", "dynamics ax", "ax 2012",
        "axapta", "microsoft axapta"
    ],
    "Windows Server": [
        "windows server", "windows server 2022", "windows server 2019",
        "windows server 2016", "windows server 2012", "windows server 2008",
        "active directory", "iis", "internet information services",
        "windows domain controller"
    ],
    "OpenVPN": [
        "openvpn", "open vpn", "openvpn access server",
        "openvpn community", "openvpn connect"
    ],

    # ── New products from CERT-FR / Cyber.gc.ca feeds ──────────────────
    "Cisco": [
        "cisco", "cisco ios", "cisco nx-os", "cisco asa", "cisco firepower",
        "cisco catalyst", "cisco sd-wan", "cisco webex", "cisco anyconnect",
        "cisco umbrella", "cisco meraki", "cisco jabber", "cisco rv",
        "cisco prime", "cisco secure", "cisco unity", "cisco telepresence"
    ],
    "VMware": [
        "vmware", "vsphere", "vcenter", "esxi", "vmware horizon",
        "vmware workstation", "vmware fusion", "vmware nsx",
        "vmware aria", "vmware tanzu", "vmware tools"
    ],
}

# Optional: aliases to canonical product names
PRODUCT_ALIASES = {
    "edge": "Microsoft Edge",
    "firefox": "Mozilla Firefox",
    "chrome": "Google Chrome",
    "chromium": "Google Chrome",
    "fortios": "Fortinet",
    "fortigate": "Fortinet",
    "mssql": "SQL Server",
    "oracle": "Oracle Database",
    "d365": "Microsoft Dynamics 365",
    "teams": "Microsoft Office",
    "sharepoint": "Microsoft Office",
    "keepass": "Keepass2",
    "chatgpt": "OpenAI",
    "axapta": "Microsoft AX 2012",
    "almalinux": "ALMA",
}
