# Features Planificadas

## Sistema de Privacidad
- ✅ Posts Privados
- ✅ Perfil privado
- ✅ Visibilidad solo para seguidores
- ✅ Comentarios solo si el posts es publico 

## Sistema de Feed
- [ ] Crear Ranking - Feed
  - ✅ Agregar endpoints para traer posts de diferentes fuentes
  - ✅ Posts de personas que sigues
  - ✅ Posts populares de amigos que dieron like
  - ✅ Posts con muchos likes
  - ✅ Posts populares por hashtags
  - ✅ Registrar interacciones de usuario con Posts

## Hashtags
- ✅ Agregar sistema de Hashtag 
- ✅ Recomendacion basica basada en ultimos hashtags reaccionados o hashtags secundarios
- ✅ Trending hashtags

## Sistema de Comentarios
- ✅ Crear endpoint para agregar comentarios a posts
- ✅ Editar comentarios propios
- ✅ Eliminar comentarios propios
- ✅ Validar permisos de usuario
- ✅ Modelo/DTO para comentarios
- ✅ Repositorio para comentarios en base de datos
- ✅ Crear arbol de herarquia
- ✅ Cargar arbol de herarquia de un post

## Sistema de Amigos
- [ ] Recomendacion de personas que quizas conozcas basado en tus seguidores
- [ ] Recomedaciones de seguidos por seguidores
- [ ] Busqueda de Usernames similares
- [ ] Grafo de amigos en profundidad
- [ ] Recomendacion de personas por peso de aristas

## Guardar Posts
- [ ] Endpoint para guardar/favoritar posts
- [ ] Endpoint para obtener posts guardados del usuario
- [ ] Endpoint para eliminar post de guardados
- [ ] Tabla de relación usuario-posts guardados
- [ ] Validar que no se guarde el mismo post dos veces

## Compartir Posts
- [ ] Endpoint para compartir posts
- [ ] Registrar historia de compartidos

## Subida de Archivos
- [ ] Endpoint para subir archivos/imágenes
- [ ] Validar tipo y tamaño de archivo
- [ ] Almacenar archivos en servidor/cloud
- [ ] Asociar archivos a posts
- [ ] Servir archivos descargables
- [ ] Limitar tamaño máximo de carga
- [ ] Limpiar archivos huérfanos (sin referencia)

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

## Privacidad y Control
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

## Estados de Implementación
- ⬜ No iniciado
- 🟨 En progreso
- ✅ Completado
