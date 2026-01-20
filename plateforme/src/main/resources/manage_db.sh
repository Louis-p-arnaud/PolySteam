#!/bin/bash

# Script de gestion de la base de données PolySteam
# Usage: ./manage_db.sh [command]

DB_CONTAINER="polysteam-db"
DB_NAME="polysteam"
DB_USER="polysteam_user"
DB_PASSWORD="PolySteam2026!"

# Couleurs pour l'affichage
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

function print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

function print_error() {
    echo -e "${RED}✗ $1${NC}"
}

function print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

function show_help() {
    echo "Usage: ./manage_db.sh [command]"
    echo ""
    echo "Commandes disponibles:"
    echo "  start           - Démarrer le conteneur PostgreSQL"
    echo "  stop            - Arrêter le conteneur PostgreSQL"
    echo "  restart         - Redémarrer le conteneur PostgreSQL"
    echo "  status          - Voir le statut du conteneur"
    echo "  logs            - Afficher les logs"
    echo "  connect         - Se connecter à PostgreSQL (psql)"
    echo "  init            - Initialiser la base avec le script SQL"
    echo "  backup          - Créer une sauvegarde"
    echo "  restore [file]  - Restaurer une sauvegarde"
    echo "  clean           - Supprimer complètement le conteneur et les volumes"
    echo "  tables          - Lister toutes les tables"
    echo "  stats           - Afficher des statistiques"
    echo "  help            - Afficher cette aide"
}

function start_container() {
    print_info "Démarrage du conteneur PostgreSQL..."
    docker compose up -d
    if [ $? -eq 0 ]; then
        print_success "Conteneur démarré avec succès"
        sleep 3
        docker ps | grep polysteam
    else
        print_error "Erreur lors du démarrage"
    fi
}

function stop_container() {
    print_info "Arrêt du conteneur PostgreSQL..."
    docker compose down
    if [ $? -eq 0 ]; then
        print_success "Conteneur arrêté avec succès"
    else
        print_error "Erreur lors de l'arrêt"
    fi
}

function restart_container() {
    print_info "Redémarrage du conteneur PostgreSQL..."
    docker compose restart
    if [ $? -eq 0 ]; then
        print_success "Conteneur redémarré avec succès"
    else
        print_error "Erreur lors du redémarrage"
    fi
}

function show_status() {
    print_info "Statut du conteneur:"
    docker ps -a | grep polysteam
    echo ""
    print_info "Statistiques Docker:"
    docker stats --no-stream $DB_CONTAINER 2>/dev/null || print_error "Conteneur non trouvé"
}

function show_logs() {
    print_info "Logs du conteneur (Ctrl+C pour quitter):"
    docker logs -f $DB_CONTAINER
}

function connect_db() {
    print_info "Connexion à PostgreSQL..."
    docker exec -it $DB_CONTAINER psql -U $DB_USER -d $DB_NAME
}

function init_db() {
    print_info "Initialisation de la base de données..."
    if [ ! -f "src/main/resources/database_polysteam.sql" ]; then
        print_error "Fichier database_polysteam.sql non trouvé"
        exit 1
    fi

    docker exec -i $DB_CONTAINER psql -U $DB_USER -d $DB_NAME < src/main/resources/database_polysteam.sql
    if [ $? -eq 0 ]; then
        print_success "Base de données initialisée avec succès"
    else
        print_error "Erreur lors de l'initialisation"
    fi
}

function backup_db() {
    BACKUP_FILE="backup_polysteam_$(date +%Y%m%d_%H%M%S).sql"
    print_info "Création d'une sauvegarde: $BACKUP_FILE"
    docker exec $DB_CONTAINER pg_dump -U $DB_USER $DB_NAME > $BACKUP_FILE
    if [ $? -eq 0 ]; then
        print_success "Sauvegarde créée: $BACKUP_FILE"
    else
        print_error "Erreur lors de la sauvegarde"
    fi
}

function restore_db() {
    if [ -z "$1" ]; then
        print_error "Veuillez spécifier le fichier de sauvegarde"
        echo "Usage: ./manage_db.sh restore [fichier.sql]"
        exit 1
    fi

    if [ ! -f "$1" ]; then
        print_error "Fichier $1 non trouvé"
        exit 1
    fi

    print_info "Restauration de la sauvegarde: $1"
    docker exec -i $DB_CONTAINER psql -U $DB_USER -d $DB_NAME < "$1"
    if [ $? -eq 0 ]; then
        print_success "Sauvegarde restaurée avec succès"
    else
        print_error "Erreur lors de la restauration"
    fi
}

function clean_all() {
    read -p "⚠️  Voulez-vous vraiment supprimer le conteneur et toutes les données ? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "Suppression du conteneur et des volumes..."
        docker compose down -v
        docker volume rm polysteam_pgdata 2>/dev/null
        print_success "Nettoyage terminé"
    else
        print_info "Opération annulée"
    fi
}

function list_tables() {
    print_info "Liste des tables:"
    docker exec $DB_CONTAINER psql -U $DB_USER -d $DB_NAME -c "\dt"
    echo ""
    print_info "Liste des vues:"
    docker exec $DB_CONTAINER psql -U $DB_USER -d $DB_NAME -c "\dv"
}

function show_stats() {
    print_info "Statistiques de la base de données:"
    docker exec $DB_CONTAINER psql -U $DB_USER -d $DB_NAME <<-EOF
        SELECT 'Éditeurs' AS table_name, COUNT(*) AS nombre FROM editeur
        UNION ALL
        SELECT 'Jeux', COUNT(*) FROM jeu_catalogue
        UNION ALL
        SELECT 'Extensions', COUNT(*) FROM extension
        UNION ALL
        SELECT 'Joueurs', COUNT(*) FROM joueur
        UNION ALL
        SELECT 'Évaluations', COUNT(*) FROM evaluation
        UNION ALL
        SELECT 'Incidents', COUNT(*) FROM rapport_incident
        UNION ALL
        SELECT 'Patches', COUNT(*) FROM patch;
EOF
}

# Main script
case "$1" in
    start)
        start_container
        ;;
    stop)
        stop_container
        ;;
    restart)
        restart_container
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs
        ;;
    connect)
        connect_db
        ;;
    init)
        init_db
        ;;
    backup)
        backup_db
        ;;
    restore)
        restore_db "$2"
        ;;
    clean)
        clean_all
        ;;
    tables)
        list_tables
        ;;
    stats)
        show_stats
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Commande inconnue: $1"
        echo ""
        show_help
        exit 1
        ;;
esac

