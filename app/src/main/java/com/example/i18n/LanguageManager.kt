package com.example.i18n

object LanguageManager {

    data class LanguageOption(
        val code: String,
        val displayName: String,
        val nativeName: String
    )

    val supportedLanguages = listOf(
        LanguageOption("AUTO", "Theo hệ thống (System)", "System Default"),
        LanguageOption("vi", "Tiếng Việt", "Tiếng Việt"),
        LanguageOption("en", "English", "English"),
        LanguageOption("zh-CN", "简体中文", "简体中文"),
        LanguageOption("zh-TW", "繁體中文", "繁體中文"),
        LanguageOption("fr", "Français", "Français"),
        LanguageOption("ja", "日本語", "日本語"),
        LanguageOption("ko", "한국어", "한국어"),
        LanguageOption("es", "Español", "Español"),
        LanguageOption("pt", "Português", "Português"),
        LanguageOption("de", "Deutsch", "Deutsch"),
        LanguageOption("ru", "Русский", "Русский")
    )

    private val translations = mapOf(
        // Tabs
        "tab_dashboard" to mapOf(
            "vi" to "Bảng điều khiển", "en" to "Dashboard", "zh-CN" to "控制台", "zh-TW" to "控制台",
            "fr" to "Tableau de bord", "ja" to "ダッシュボード", "ko" to "대시보드", "es" to "Panel",
            "pt" to "Painel", "de" to "Übersicht", "ru" to "Панель"
        ),
        "tab_apps" to mapOf(
            "vi" to "Ứng dụng", "en" to "Apps", "zh-CN" to "应用管理", "zh-TW" to "應用管理",
            "fr" to "Applications", "ja" to "アプリ", "ko" to "앱 목록", "es" to "Aplicaciones",
            "pt" to "Aplicativos", "de" to "Apps", "ru" to "Приложения"
        ),
        "tab_diagnostics" to mapOf(
            "vi" to "Chẩn đoán", "en" to "Diagnostics", "zh-CN" to "网络诊断", "zh-TW" to "網絡診斷",
            "fr" to "Diagnostics", "ja" to "診断", "ko" to "진단", "es" to "Diagnóstico",
            "pt" to "Diagnóstico", "de" to "Diagnose", "ru" to "Диагностика"
        ),
        "tab_history" to mapOf(
            "vi" to "Nhật ký", "en" to "History Logs", "zh-CN" to "事件日志", "zh-TW" to "事件日誌",
            "fr" to "Historique", "ja" to "履歴", "ko" to "기록 로그", "es" to "Historial",
            "pt" to "Histórico", "de" to "Verlauf", "ru" to "Журнал"
        ),
        "tab_settings" to mapOf(
            "vi" to "Cài đặt", "en" to "Settings", "zh-CN" to "设置", "zh-TW" to "設定",
            "fr" to "Paramètres", "ja" to "設定", "ko" to "설정", "es" to "Ajustes",
            "pt" to "Configurações", "de" to "Einstellungen", "ru" to "Настройки"
        ),

        // Dashboard
        "status_active" to mapOf(
            "vi" to "Đang bảo vệ", "en" to "Guard Active", "zh-CN" to "保护中", "zh-TW" to "保護中",
            "fr" to "Protection active", "ja" to "保護中", "ko" to "보호 활성화됨", "es" to "Protección activa",
            "pt" to "Proteção ativa", "de" to "Schutz aktiv", "ru" to "Защита активна"
        ),
        "status_inactive" to mapOf(
            "vi" to "Đang tạm dừng", "en" to "Guard Inactive", "zh-CN" to "已暂停", "zh-TW" to "已暫停",
            "fr" to "En pause", "ja" to "一時停止", "ko" to "일시 중지됨", "es" to "En pausa",
            "pt" to "Pausado", "de" to "Pausiert", "ru" to "Приостановлено"
        ),
        "btn_repair_now" to mapOf(
            "vi" to "Sửa lỗi ngay", "en" to "Repair Now", "zh-CN" to "立即修复", "zh-TW" to "立即修復",
            "fr" to "Réparer maintenant", "ja" to "今すぐ修復", "ko" to "지금 복구", "es" to "Reparar ahora",
            "pt" to "Reparar agora", "de" to "Jetzt reparieren", "ru" to "Исправить сейчас"
        ),
        "repairing" to mapOf(
            "vi" to "Đang làm mới socket...", "en" to "Refreshing socket...", "zh-CN" to "正在刷新连接...", "zh-TW" to "正在刷新連線...",
            "fr" to "Actualisation du socket...", "ja" to "ソケット更新中...", "ko" to "소켓 갱신 중...", "es" to "Actualizando socket...",
            "pt" to "Atualizando socket...", "de" to "Aktualisiere Socket...", "ru" to "Обновление сокета..."
        ),
        "socket_online" to mapOf(
            "vi" to "Socket Google Push: Kết nối tốt", "en" to "Google Push Socket: Healthy", "zh-CN" to "Google 推送连接: 畅通", "zh-TW" to "Google 推送連線: 暢通",
            "fr" to "Socket Google Push: Connecté", "ja" to "Google Push Socket: 正常", "ko" to "Google Push 소켓: 연결됨", "es" to "Socket Google Push: Conectado",
            "pt" to "Socket Google Push: Conectado", "de" to "Google Push Socket: Verbunden", "ru" to "Google Push сокет: Подключен"
        ),
        "socket_offline" to mapOf(
            "vi" to "Socket Google Push: Chưa kết nối", "en" to "Google Push Socket: Disconnected", "zh-CN" to "Google 推送连接: 断开", "zh-TW" to "Google 推送連線: 斷開",
            "fr" to "Socket Google Push: Déconnecté", "ja" to "Google Push Socket: 切断", "ko" to "Google Push 소켓: 연결 끊김", "es" to "Socket Google Push: Desconectado",
            "pt" to "Socket Google Push: Desconectado", "de" to "Google Push Socket: Getrennt", "ru" to "Google Push сокет: Отключен"
        ),
        "backup_interval_title" to mapOf(
            "vi" to "Chu kỳ kiểm tra ngầm dự phòng", "en" to "Backup In-Process Check Interval", "zh-CN" to "备用后台检测周期", "zh-TW" to "備用背景檢測週期",
            "fr" to "Intervalle de contrôle de secours", "ja" to "バックアップ内部確認間隔", "ko" to "예비 내부 확인 주기", "es" to "Intervalo de verificación de respaldo",
            "pt" to "Intervalo de verificação de backup", "de" to "Backup-Hintergrundprüfintervall", "ru" to "Интервал резервной проверки"
        ),
        "switch_auto_guard" to mapOf(
            "vi" to "Tự động bảo vệ thông minh", "en" to "Smart Auto Guard", "zh-CN" to "智能自动守护", "zh-TW" to "智能自動守護",
            "fr" to "Garde automatique intelligente", "ja" to "スマート自動保護", "ko" to "스마트 자동 보호", "es" to "Protección automática inteligente",
            "pt" to "Guardião automático inteligente", "de" to "Intelligenter automatischer Schutz", "ru" to "Умная автозащита"
        ),
        "switch_auto_guard_desc" to mapOf(
            "vi" to "Làm mới socket khi khôi phục mạng hoặc bật sáng màn hình",
            "en" to "Refresh MCS socket upon network recovery or screen on",
            "zh-CN" to "网络恢复或亮屏时自动刷新 MCS 推送连接",
            "zh-TW" to "網絡恢復或亮屏時自動刷新 MCS 推送連線",
            "fr" to "Actualiser le socket MCS au rétablissement du réseau ou à l'allumage",
            "ja" to "ネットワーク復帰時または画面点灯時に自動更新",
            "ko" to "네트워크 복구 또는 화면 켜짐 시 소켓 자동 갱신",
            "es" to "Actualizar socket MCS al restaurar red o encender pantalla",
            "pt" to "Atualizar socket MCS ao restaurar rede ou ligar tela",
            "de" to "MCS-Socket bei Netzwerkwiederherstellung oder Bildschirm-An aktualisieren",
            "ru" to "Обновлять сокет MCS при восстановлении сети или включении экрана"
        ),
        "switch_notification" to mapOf(
            "vi" to "Thông báo chạy ngầm ưu tiên", "en" to "Persistent Foreground Notification", "zh-CN" to "前台常驻静音通知", "zh-TW" to "前台常駐靜音通知",
            "fr" to "Notification permanente en avant-plan", "ja" to "優先バックグラウンド通知", "ko" to "상시 백그라운드 알림", "es" to "Notificación persistente en primer plano",
            "pt" to "Notificação persistente em primeiro plano", "de" to "Dauerhafte Vordergrund-Benachrichtigung", "ru" to "Постоянное фоновое уведомление"
        ),
        "switch_notification_desc" to mapOf(
            "vi" to "Kênh im lặng giúp HyperOS không thu hồi RAM của ứng dụng",
            "en" to "Silent channel preventing HyperOS from reclaiming app RAM",
            "zh-CN" to "静音通道防止 HyperOS 误清理后台进程",
            "zh-TW" to "靜音通道防止 HyperOS 誤清理背景進程",
            "fr" to "Canal silencieux empêchant HyperOS de fermer le processus",
            "ja" to "HyperOS によるプロセスの強制終了を防止",
            "ko" to "HyperOS가 앱 프로세스를 종료하지 못하도록 방지",
            "es" to "Canal silencioso que evita que HyperOS cierre el proceso",
            "pt" to "Canal silencioso que evita que o HyperOS encerre o processo",
            "de" to "Lautloser Kanal, der verhindert, dass HyperOS den Prozess beendet",
            "ru" to "Тихий канал, предотвращающий выгрузку процесса HyperOS"
        ),
        "hyperos_shortcuts" to mapOf(
            "vi" to "Lối tắt cấu hình HyperOS", "en" to "HyperOS System Shortcuts", "zh-CN" to "HyperOS 快捷配置", "zh-TW" to "HyperOS 快捷配置",
            "fr" to "Raccourcis système HyperOS", "ja" to "HyperOS システムショートカット", "ko" to "HyperOS 시스템 바로가기", "es" to "Accesos directos de HyperOS",
            "pt" to "Atalhos do sistema HyperOS", "de" to "HyperOS-Systemverknüpfungen", "ru" to "Быстрые настройки HyperOS"
        ),
        "btn_autostart" to mapOf(
            "vi" to "Cấu hình tự khởi chạy", "en" to "Autostart Settings", "zh-CN" to "自启动管理", "zh-TW" to "自啟動管理",
            "fr" to "Gestion démarrage auto", "ja" to "自動起動管理", "ko" to "자동 시작 관리", "es" to "Inicio automático",
            "pt" to "Início automático", "de" to "Autostart-Verwaltung", "ru" to "Автозапуск"
        ),
        "btn_battery" to mapOf(
            "vi" to "Tối ưu hóa pin", "en" to "Battery Optimization", "zh-CN" to "省电优化", "zh-TW" to "省電優化",
            "fr" to "Optimisation batterie", "ja" to "バッテリー最適化", "ko" to "배터리 최적화", "es" to "Optimización de batería",
            "pt" to "Otimização de bateria", "de" to "Akkuoptimierung", "ru" to "Оптимизация батареи"
        ),
        "btn_write_settings" to mapOf(
            "vi" to "Sửa đổi cài đặt", "en" to "Modify Settings", "zh-CN" to "修改系统设置", "zh-TW" to "修改系統設定",
            "fr" to "Modifier paramètres", "ja" to "システム設定の変更", "ko" to "시스템 설정 변경", "es" to "Modificar ajustes",
            "pt" to "Modificar configurações", "de" to "Systemeinstellungen ändern", "ru" to "Изменение настроек"
        ),
        "btn_clear_battery_performance" to mapOf(
            "vi" to "Xóa dữ liệu Pin & Hiệu suất", "en" to "Clear Battery & Performance Data", "zh-CN" to "清除电量与性能数据", "zh-TW" to "清除電量與效能資料",
            "fr" to "Effacer données Batterie & Perf", "ja" to "バッテリーとパフォーマンスのデータ消去", "ko" to "배터리 및 성능 데이터 삭제", "es" to "Borrar datos de Batería y Rendimiento",
            "pt" to "Limpar dados de Bateria e Desempenho", "de" to "Akku- & Leistungsdaten löschen", "ru" to "Сброс данных Питание и производительность"
        ),
        "btn_clear_battery_performance_desc" to mapOf(
            "vi" to "Mở trang Thông tin ứng dụng Pin & Hiệu suất (com.miui.powerkeeper) để bạn bấm 'Xóa tất cả dữ liệu' nhằm đặt lại giới hạn ngắt tiến trình HyperOS",
            "en" to "Open Battery & Performance App Info (com.miui.powerkeeper) to tap 'Clear all data' to reset HyperOS background throttling",
            "zh-CN" to "跳转至“电量与性能”应用信息界面，点击“清除全部数据”以重置 HyperOS 后台杀进程策略",
            "zh-TW" to "跳轉至「電量與效能」應用資訊介面，點選「清除全部資料」以重設 HyperOS 背景清理策略",
            "fr" to "Ouvre les infos de l'application Batterie pour effacer toutes les données et réinitialiser les restrictions",
            "ja" to "「バッテリーとパフォーマンス」の設定画面を開き、すべてのデータを消去して制限をリセット",
            "ko" to "배터리 및 성능 앱 정보로 이동하여 '모든 데이터 삭제'를 눌러 백그라운드 제한을 초기화합니다",
            "es" to "Abre Información de Batería y Rendimiento para borrar todos los datos y reiniciar límites",
            "pt" to "Abre as informações do app Bateria e Desempenho para limpar dados e redefinir restrições",
            "de" to "Öffnet Akku- & Leistung-App-Info, um 'Alle Daten löschen' zu wählen",
            "ru" to "Открыть сведения о приложении 'Питание и производительность' для очистки всех данных"
        ),

        // FCM Apps
        "apps_title" to mapOf(
            "vi" to "Trợ lý ứng dụng FCM", "en" to "FCM Push Client Apps", "zh-CN" to "FCM 推送应用助手", "zh-TW" to "FCM 推送應用助手",
            "fr" to "Applications clientes FCM", "ja" to "FCM プッシュアプリ", "ko" to "FCM 푸시 앱 도우미", "es" to "Aplicaciones FCM",
            "pt" to "Aplicativos FCM", "de" to "FCM-Push-Apps", "ru" to "FCM-клиенты"
        ),
        "apps_desc" to mapOf(
            "vi" to "Quét các ứng dụng dùng Google Push và kiểm tra quyền Tự khởi chạy HyperOS",
            "en" to "Scan apps using Google Push and check HyperOS Autostart permissions",
            "zh-CN" to "扫描使用 Google 推送的应用并检测 HyperOS 自启动权限",
            "zh-TW" to "掃描使用 Google 推送的應用並檢測 HyperOS 自啟動權限",
            "fr" to "Scannez les applications FCM et vérifiez les autorisations de démarrage auto",
            "ja" to "Google Push を利用するアプリをスキャンし自動起動状態を確認",
            "ko" to "Google 푸시를 사용하는 앱을 스캔하고 HyperOS 자동 시작 권한 확인",
            "es" to "Escanear aplicaciones con Google Push y verificar inicio automático",
            "pt" to "Escanear apps com Google Push e verificar permissões de autoinício",
            "de" to "Google Push-Apps scannen und HyperOS-Autostart-Berechtigungen prüfen",
            "ru" to "Сканирование приложений с Google Push и проверка автозапуска HyperOS"
        ),
        "btn_config_all" to mapOf(
            "vi" to "Cấu hình tất cả trong HyperOS", "en" to "Configure All in HyperOS", "zh-CN" to "一键配置全部自启动", "zh-TW" to "一鍵配置全部自啟動",
            "fr" to "Configurer tout dans HyperOS", "ja" to "HyperOSですべて設定", "ko" to "HyperOS에서 전체 설정", "es" to "Configurar todo en HyperOS",
            "pt" to "Configurar tudo no HyperOS", "de" to "Alles in HyperOS konfigurieren", "ru" to "Настроить всё в HyperOS"
        ),
        "search_hint" to mapOf(
            "vi" to "Tìm kiếm ứng dụng...", "en" to "Search apps...", "zh-CN" to "搜索应用...", "zh-TW" to "搜尋應用...",
            "fr" to "Rechercher...", "ja" to "アプリを検索...", "ko" to "앱 검색...", "es" to "Buscar aplicaciones...",
            "pt" to "Pesquisar aplicativos...", "de" to "Apps suchen...", "ru" to "Поиск приложений..."
        ),
        "autostart_enabled" to mapOf(
            "vi" to "Đã bật", "en" to "Enabled", "zh-CN" to "已开启", "zh-TW" to "已開啟",
            "fr" to "Activé", "ja" to "有効", "ko" to "활성화됨", "es" to "Habilitado",
            "pt" to "Ativado", "de" to "Aktiviert", "ru" to "Включено"
        ),
        "autostart_partial" to mapOf(
            "vi" to "Một phần", "en" to "Partial", "zh-CN" to "部分允许", "zh-TW" to "部分允許",
            "fr" to "Partiel", "ja" to "一部許可", "ko" to "부분 허용", "es" to "Parcial",
            "pt" to "Parcial", "de" to "Teilweise", "ru" to "Частично"
        ),
        "autostart_disabled" to mapOf(
            "vi" to "Đã tắt", "en" to "Disabled", "zh-CN" to "已关闭", "zh-TW" to "已關閉",
            "fr" to "Désactivé", "ja" to "無効", "ko" to "비활성화됨", "es" to "Deshabilitado",
            "pt" to "Desativado", "de" to "Deaktiviert", "ru" to "Отключено"
        ),
        "autostart_unknown" to mapOf(
            "vi" to "Chưa rõ", "en" to "Unknown", "zh-CN" to "未知", "zh-TW" to "未知",
            "fr" to "Inconnu", "ja" to "不明", "ko" to "알 수 없음", "es" to "Desconocido",
            "pt" to "Desconhecido", "de" to "Unbekannt", "ru" to "Неизвестно"
        ),

        // Diagnostics
        "diag_title" to mapOf(
            "vi" to "Chẩn đoán kết nối Socket", "en" to "Push Socket Diagnostics", "zh-CN" to "推送连接诊断", "zh-TW" to "推送連線診斷",
            "fr" to "Diagnostics de connexion", "ja" to "プッシュ接続診断", "ko" to "푸시 소켓 진단", "es" to "Diagnóstico de Socket",
            "pt" to "Diagnóstico do Socket", "de" to "Push-Socket-Diagnose", "ru" to "Диагностика сокета"
        ),
        "diag_desc" to mapOf(
            "vi" to "Kiểm tra trực tiếp cổng TCP socket mtalk.google.com:5228 và độ trễ DNS",
            "en" to "Directly test TCP socket to mtalk.google.com:5228 and DNS latency",
            "zh-CN" to "直接测试 mtalk.google.com:5228 的 TCP 连接与 DNS 延迟",
            "zh-TW" to "直接測試 mtalk.google.com:5228 的 TCP 連線與 DNS 延遲",
            "fr" to "Test direct du socket TCP mtalk.google.com:5228 et latence DNS",
            "ja" to "mtalk.google.com:5228 への直接TCP接続とDNSレイテンシをテスト",
            "ko" to "mtalk.google.com:5228 TCP 소켓 및 DNS 대기 시간 직접 테스트",
            "es" to "Probar directamente el socket TCP a mtalk.google.com:5228 y latencia DNS",
            "pt" to "Testar diretamente o socket TCP para mtalk.google.com:5228 e latência de DNS",
            "de" to "Direkter TCP-Socket-Test zu mtalk.google.com:5228 und DNS-Latenz",
            "ru" to "Прямой тест TCP-сокета к mtalk.google.com:5228 и задержки DNS"
        ),
        "btn_run_test" to mapOf(
            "vi" to "Bắt đầu kiểm tra socket", "en" to "Run Socket Test", "zh-CN" to "开始连接测试", "zh-TW" to "開始連線測試",
            "fr" to "Lancer le test de socket", "ja" to "接続テストを開始", "ko" to "소켓 테스트 실행", "es" to "Ejecutar prueba de socket",
            "pt" to "Executar teste de socket", "de" to "Socket-Test starten", "ru" to "Запустить тест сокета"
        ),
        "btn_gcm_diag" to mapOf(
            "vi" to "Chẩn đoán Dịch vụ Google Play (*#*#426#*#*)", "en" to "Google Play Services Diagnostics (*#*#426#*#*)",
            "zh-CN" to "打开 Google 服务诊断 (*#*#426#*#*)", "zh-TW" to "開啟 Google 服務診斷 (*#*#426#*#*)",
            "fr" to "Diagnostics Google Play (*#*#426#*#*)", "ja" to "Google Play 開発者サービス診断 (*#*#426#*#*)",
            "ko" to "Google Play 서비스 진단 (*#*#426#*#*)", "es" to "Diagnóstico de Google Play (*#*#426#*#*)",
            "pt" to "Diagnóstico do Google Play (*#*#426#*#*)", "de" to "Google Play-Dienste Diagnose (*#*#426#*#*)",
            "ru" to "Диагностика сервисов Google (*#*#426#*#*)"
        ),
        "dns_latency" to mapOf(
            "vi" to "Độ trễ phân giải DNS", "en" to "DNS Resolve Latency", "zh-CN" to "DNS 解析延迟", "zh-TW" to "DNS 解析延遲",
            "fr" to "Latence résolution DNS", "ja" to "DNS解決レイテンシ", "ko" to "DNS 확인 지연 시간", "es" to "Latencia DNS",
            "pt" to "Latência de DNS", "de" to "DNS-Auflösungslatenz", "ru" to "Задержка DNS"
        ),
        "tcp_latency" to mapOf(
            "vi" to "Độ trễ bắt tay TCP (:5228)", "en" to "TCP Handshake Latency (:5228)", "zh-CN" to "TCP 握手延迟 (:5228)", "zh-TW" to "TCP 握手延遲 (:5228)",
            "fr" to "Latence TCP (:5228)", "ja" to "TCP ハンドシェイク遅延 (:5228)", "ko" to "TCP 핸드셰이크 지연 (:5228)", "es" to "Latencia TCP (:5228)",
            "pt" to "Latência TCP (:5228)", "de" to "TCP-Handshake-Latenz (:5228)", "ru" to "Задержка TCP (:5228)"
        ),
        "resolved_ip" to mapOf(
            "vi" to "Địa chỉ IP máy chủ", "en" to "Server IP Address", "zh-CN" to "解析 IP 地址", "zh-TW" to "解析 IP 位址",
            "fr" to "Adresse IP serveur", "ja" to "サーバーIPアドレス", "ko" to "서버 IP 주소", "es" to "Dirección IP",
            "pt" to "Endereço IP", "de" to "Server-IP-Adresse", "ru" to "IP-адрес сервера"
        ),

        // History
        "history_title" to mapOf(
            "vi" to "Nhật ký độ trễ & Mất kết nối", "en" to "Push Latency & Disconnection Log", "zh-CN" to "延迟与断开记录", "zh-TW" to "延遲與斷開記錄",
            "fr" to "Journal de latence & coupures", "ja" to "レイテンシと切断ログ", "ko" to "지연 및 연결 끊김 로그", "es" to "Registro de latencia y desconexión",
            "pt" to "Registro de latência e desconexões", "de" to "Latenz- & Trennungsverlauf", "ru" to "Журнал задержек и отключений"
        ),
        "btn_clear_logs" to mapOf(
            "vi" to "Xóa nhật ký", "en" to "Clear Logs", "zh-CN" to "清除日志", "zh-TW" to "清除日誌",
            "fr" to "Effacer", "ja" to "ログ消去", "ko" to "기록 삭제", "es" to "Limpiar",
            "pt" to "Limpar", "de" to "Löschen", "ru" to "Очистить"
        ),
        "stat_total" to mapOf(
            "vi" to "Tổng sự kiện", "en" to "Total Events", "zh-CN" to "事件总数", "zh-TW" to "事件總數",
            "fr" to "Total", "ja" to "総イベント", "ko" to "총 이벤트", "es" to "Total",
            "pt" to "Total", "de" to "Gesamt", "ru" to "Всего"
        ),
        "stat_success" to mapOf(
            "vi" to "Heartbeat OK", "en" to "Heartbeat OK", "zh-CN" to "成功", "zh-TW" to "成功",
            "fr" to "Réussis", "ja" to "成功", "ko" to "성공", "es" to "Exitosos",
            "pt" to "Sucesso", "de" to "Erfolgreich", "ru" to "Успешно"
        ),
        "stat_avg_latency" to mapOf(
            "vi" to "Độ trễ TB", "en" to "Avg Latency", "zh-CN" to "平均延迟", "zh-TW" to "平均延遲",
            "fr" to "Latence moy.", "ja" to "平均遅延", "ko" to "평균 지연", "es" to "Latencia prom.",
            "pt" to "Latência média", "de" to "Ø Latenz", "ru" to "Средн. задержка"
        ),
        "empty_logs" to mapOf(
            "vi" to "Chưa có sự kiện nào. Hãy nhấn 'Sửa lỗi ngay' hoặc chạy Chẩn đoán để bắt đầu ghi nhật ký.",
            "en" to "No logs recorded yet. Tap 'Repair Now' or run Diagnostics to start logging.",
            "zh-CN" to "暂无事件日志。点击“立即修复”或运行网络诊断开始记录。",
            "zh-TW" to "暫無事件日誌。點選「立即修復」或執行網絡診斷開始記錄。",
            "fr" to "Aucun événement enregistré. Appuyez sur « Réparer maintenant ».",
            "ja" to "ログはまだありません。「今すぐ修復」を押してください。",
            "ko" to "아직 기록된 로그가 없습니다. '지금 복구'를 눌러 시작하세요.",
            "es" to "Aún no hay registros. Toque 'Reparar ahora' para comenzar.",
            "pt" to "Nenhum registro ainda. Toque em 'Reparar agora' para iniciar.",
            "de" to "Noch keine Protokolle vorhanden. Tippen Sie auf 'Jetzt reparieren'.",
            "ru" to "Записей пока нет. Нажмите 'Исправить сейчас' для начала."
        ),

        // Settings & Guide
        "guide_title" to mapOf(
            "vi" to "Hướng dẫn cài đặt nhanh (4 bước)", "en" to "Quick Setup Guide (4 Steps)", "zh-CN" to "快速配置指南 (4 步)", "zh-TW" to "快速設定指南 (4 步)",
            "fr" to "Guide d'installation rapide (4 étapes)", "ja" to "クイック設定ガイド (4ステップ)", "ko" to "빠른 설정 가이드 (4단계)", "es" to "Guía rápida (4 pasos)",
            "pt" to "Guia de instalação rápida (4 passos)", "de" to "Schnellanleitung (4 Schritte)", "ru" to "Быстрая настройка (4 шага)"
        ),
        "troubleshoot_title" to mapOf(
            "vi" to "Mẹo khắc phục ứng dụng chậm thông báo", "en" to "Fixing Delayed App Notifications", "zh-CN" to "解决单款应用推送延迟技巧", "zh-TW" to "解決單款應用推送延遲技巧",
            "fr" to "Conseils de dépannage par application", "ja" to "特定アプリの通知遅延対処法", "ko" to "특정 앱 알림 지연 해결 팁", "es" to "Solución para retrasos de notificaciones",
            "pt" to "Dicas para corrigir atrasos em apps", "de" to "Tipps bei verzögerten Benachrichtigungen", "ru" to "Устранение задержек уведомлений"
        ),
        "theme_selector" to mapOf(
            "vi" to "Giao diện (Chế độ)", "en" to "Appearance (Theme)", "zh-CN" to "界面主题", "zh-TW" to "介面主題",
            "fr" to "Thème d'affichage", "ja" to "テーマ外観", "ko" to "테마 모드", "es" to "Tema",
            "pt" to "Tema da interface", "de" to "Designmodus", "ru" to "Тема оформления"
        ),
        "lang_selector" to mapOf(
            "vi" to "Ngôn ngữ ứng dụng", "en" to "App Language", "zh-CN" to "应用语言", "zh-TW" to "應用語言",
            "fr" to "Langue de l'application", "ja" to "アプリの言語", "ko" to "앱 언어", "es" to "Idioma de la aplicación",
            "pt" to "Idioma do aplicativo", "de" to "App-Sprache", "ru" to "Язык приложения"
        ),
        "security_assurance" to mapOf(
            "vi" to "Bảo đảm an toàn ngân hàng & tài chính", "en" to "Banking & Finance Safety Guarantee", "zh-CN" to "金融与网银安全保障", "zh-TW" to "金融與網銀安全保障",
            "fr" to "Garantie de sécurité bancaire", "ja" to "銀行・金融セキュリティ保証", "ko" to "금융 및 은행 보안 보증", "es" to "Garantía de seguridad bancaria",
            "pt" to "Garantia de segurança bancária", "de" to "Sicherheitsgarantie für Banking", "ru" to "Безопасность для банковских приложений"
        ),
        "security_assurance_desc" to mapOf(
            "vi" to "100% Không Root, Không Shizuku, Không Accessibility, Không VPN ngầm. Hoàn toàn an toàn cho VCB, Techcombank, MB, Momo, VNPay...",
            "en" to "100% No Root, No Shizuku, No Accessibility Service, No VPN. Completely safe for banking & financial apps.",
            "zh-CN" to "100% 免 Root、免 Shizuku、无无障碍服务、无后台 VPN，对手机银行与金融类应用绝对安全。",
            "zh-TW" to "100% 免 Root、免 Shizuku、無無障礙服務、無後台 VPN，對手機銀行與金融類應用絕對安全。",
            "fr" to "100% Sans Root, Sans Shizuku, Sans Accessibilité, Sans VPN. Totalement sûr pour les applications bancaires.",
            "ja" to "Root不要、Shizuku不要、ユーザー補助機能不使用、VPN不使用。銀行・金融アプリも安心。",
            "ko" to "루트 불필요, Shizuku 불필요, 접근성 서비스 미사용, VPN 미사용. 금융 앱에 100% 안전합니다.",
            "es" to "100% Sin Root, Sin Shizuku, Sin Accesibilidad, Sin VPN. Totalmente seguro para apps bancarias.",
            "pt" to "100% Sem Root, Sem Shizuku, Sem Acessibilidade, Sem VPN. Totalmente seguro para apps bancários.",
            "de" to "100% Kein Root, kein Shizuku, keine Barrierefreiheit, kein VPN. Sicher für Banking-Apps.",
            "ru" to "100% Без Root, без Shizuku, без спец. возможностей, без VPN. Безопасно для банковских приложений."
        ),
        "app_version" to mapOf(
            "vi" to "Phiên bản ứng dụng", "en" to "Application Version", "zh-CN" to "应用版本", "zh-TW" to "應用版本",
            "fr" to "Version", "ja" to "アプリバージョン", "ko" to "앱 버전", "es" to "Versión",
            "pt" to "Versão", "de" to "Version", "ru" to "Версия"
        ),
        "btn_view_readme" to mapOf(
            "vi" to "Xem tài liệu README & Hướng dẫn", "en" to "View Full README & Documentation", "zh-CN" to "查看 README 与文档", "zh-TW" to "查看 README 與文檔",
            "fr" to "Voir le README complet", "ja" to "READMEとドキュメントを表示", "ko" to "README 및 설명서 보기", "es" to "Ver README completo",
            "pt" to "Ver README completo", "de" to "Vollständige README anzeigen", "ru" to "Открыть полный README"
        )
    )

    fun getString(key: String, langCode: String): String {
        val effectiveLang = if (langCode == "AUTO") {
            val systemLocale = java.util.Locale.getDefault().language
            when (systemLocale) {
                "vi" -> "vi"
                "zh" -> if (java.util.Locale.getDefault().country.equals("TW", ignoreCase = true) ||
                    java.util.Locale.getDefault().country.equals("HK", ignoreCase = true)) "zh-TW" else "zh-CN"
                "fr" -> "fr"
                "ja" -> "ja"
                "ko" -> "ko"
                "es" -> "es"
                "pt" -> "pt"
                "de" -> "de"
                "ru" -> "ru"
                else -> "en"
            }
        } else {
            langCode
        }

        val map = translations[key] ?: return key
        return map[effectiveLang] ?: map["en"] ?: map["vi"] ?: key
    }
}
