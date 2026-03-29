# Features Planificadas

## Sistema de Feed
- [ ] Agregar endpoints para traer posts de diferentes fuentes
    - [ ] posts de amigos que dieron like
    - [ ] posts con muchos likes
- [ ] Registrar interacciones del usuario con Posts

## Hashtags
- [ ] Agregar sistema de Hashtag y recomendacion basica

## Sistema de Comentarios
- [🟨] Crear endpoint para agregar comentarios a posts
- [🟨] Obtener comentarios de un post
- [🟨] Editar comentarios propios
- [🟨] Eliminar comentarios propios
- [🟨] Validar permisos de usuario
- [🟨] Modelo/DTO para comentarios
- [🟨] Repositorio para comentarios en base de datos

## Guardar Posts
- [ ] Endpoint para guardar/favoritar posts
- [ ] Endpoint para obtener posts guardados del usuario
- [ ] Endpoint para eliminar post de guardados
- [ ] Tabla de relación usuario-posts guardados
- [ ] Validar que no se guarde el mismo post dos veces

## Compartir Posts
- [ ] Endpoint para compartir posts
- [ ] Registrar historia de compartidos
- [ ] Obtener estadísticas de compartidos
- [ ] Compartir por link/URL
- [ ] Generar código de compartición único (opcional)

## Subida de Archivos
- [ ] Endpoint para subir archivos/imágenes
- [ ] Validar tipo y tamaño de archivo
- [ ] Almacenar archivos en servidor/cloud
- [ ] Asociar archivos a posts
- [ ] Servir archivos descargables
- [ ] Limitar tamaño máximo de carga
- [ ] Limpiar archivos huérfanos (sin referencia)

## Menciones y Hashtags
- [ ] Detectar @menciones en posts
- [ ] Notificar al usuario mencionado
- [ ] Link a perfil desde menciones
- [ ] Detectar #hashtags en posts
- [ ] Crear página de hashtag
- [ ] Buscar posts por hashtag
- [ ] Trending hashtags

## Notificaciones en Tiempo Real
- [ ] Configurar WebSocket para notificaciones
- [ ] Notificar nuevo comentario en post
- [ ] Notificar mención en comentario/post
- [ ] Notificar nuevo seguidor
- [ ] Notificar cuando alguien comparte tu post
- [ ] Endpoint para obtener notificaciones
- [ ] Marcar notificaciones como leídas

## Búsqueda y Descubrimiento
- [ ] Buscar posts por texto/contenido
- [ ] Buscar usuarios por nombre/username
- [ ] Filtrar posts por categorías
- [ ] Página de trending topics
- [ ] Algoritmo de recomendaciones de posts
- [ ] Recomendaciones de usuarios a seguir

## Estadísticas de Usuario
- [ ] Obtener historial de posts del usuario
- [ ] Contar posts totales del usuario
- [ ] Estadísticas de engagement (likes, comentarios)
- [ ] Mostrar fecha de creación de cuenta

## Privacidad y Control
- [ ] Posts privados (solo seguidores)
- [ ] Endpoint para bloquear usuario
- [ ] Endpoint para desbloquear usuario
- [ ] Lista de usuarios bloqueados
- [ ] Impedir que bloqueado vea posts privados

## Reportes y Moderación
- [ ] Endpoint para reportar post
- [ ] Endpoint para reportar usuario
- [ ] Registrar motivo del reporte
- [ ] Panel admin para ver reportes
- [ ] Admin puede eliminar post reportado
- [ ] Admin puede suspender usuario

## Historial de Actividad
- [ ] Registrar última conexión del usuario
- [ ] Historial de ediciones de post
- [ ] Ver quién vio tu post
- [ ] Análisis de actividad del usuario

## Edición y Borrador de Posts
- [ ] Endpoint para editar post existente
- [ ] Guardar posts como borrador
- [ ] Obtener borradores del usuario
- [ ] Publicar borrador
- [ ] Eliminar borrador

## Thread de Posts (Conversaciones)
- [ ] Crear post con respuesta a otro post
- [ ] Obtener árbol de conversación
- [ ] Notificar cuando responden tu post
- [ ] Mostrar posts relacionados en orden

## Retweets/Re-compartir
- [ ] Endpoint para retweetear post de otro
- [ ] Obtener retweets de un post
- [ ] Mostrar quién hizo retweet
- [ ] Eliminar retweet propio

## Panel y Moderación Admin
- [ ] Endpoint admin para listar usuarios
- [ ] Endpoint admin para listar posts reportados
- [ ] Endpoint admin para suspender usuario
- [ ] Endpoint admin para eliminar post
- [ ] Estadísticas globales del sitio
- [ ] Control de roles de admin

---

## Estados de Implementación
- ⬜ No iniciado
- 🟨 En progreso
- ✅ Completado
