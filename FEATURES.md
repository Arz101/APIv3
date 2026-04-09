# Features Planificadas

## Sistema de Privacidad
- ✅ Posts Privados
- ✅ Perfil privado
- ✅ Visibilidad solo para seguidores
- ✅ Comentarios solo si el posts es publico 
- ✅ Endpoint para bloquear usuario
- ✅ Endpoint para desbloquear usuario
- ✅ Lista de usuarios bloqueados
- ✅ Impedir que bloqueado vea posts privados
- ✅ Eliminar o aceptar solicitudes de seguimiento

## Sistema de Feed
- ✅ Crear Ranking - Feed
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
- ✅ Recomedaciones de seguidos por seguidos
- ✅ Busqueda de Usernames similares
- ✅ Grafo de follows en profundidad

## Motor de Recomendaciones
- ✅ Grafo de hashtags co-ocurrentes (hashtagGraph)
- ✅ Posts indexados por tag, usuario y likes en memoria
- ✅ Hashtags con mayor afinidad por usuario (tagsLikedByUser)
- ✅ Feed basado en hashtags co-ocurrentes (createFeed)
- ✅ Scoring de posts: rankPosts() - combinar likes sociales + afinidad de hashtags + popularidad
- ✅ Incorporar followsGraph al feed (posts que gustaron a personas que sigo)
- [ ] Filtrado colaborativo: usuarios con gustos similares likearon X
- ✅ Excluir posts ya vistos del feed (usar PostViewed)
- [ ] Recomendacion de personas por peso de aristas en followsGraph

## Cache y Actualizacion en Tiempo Real
- ✅ Cache de feed por usuario (Caffeine)
- [ ] Invalidar/actualizar cache al crear un nuevo post
- [ ] Actualizar SocialDataStore en memoria al registrar un nuevo like
- [ ] Actualizar SocialDataStore en memoria al registrar un nuevo follow
- [ ] Actualizar SocialDataStore en memoria al crear un post
- [ ] Registrar vistas de posts y alimentar el motor de recomendaciones

## Guardar Posts
- ✅ Endpoint para guardar/favoritar posts
- ✅ Endpoint para obtener posts guardados del usuario
- ✅ Endpoint para eliminar post de guardados
- ✅ Tabla de relación usuario-posts guardados
- ✅ Validar que no se guarde el mismo post dos veces

## Compartir Posts
- [ ] Endpoint para compartir posts
- [ ] Endpoint para borrar post compartido

## Subida de Archivos
- ✅ Endpoint para subir archivos/imágenes
- ✅ Validar tipo y tamaño de archivo
- ✅ Asociar archivos a posts
- ✅ Limitar tamaño máximo de carga

## Notificaciones en Tiempo Real
- [ ] Configurar WebSocket para notificaciones
- [ ] Notificar nuevo comentario en post
- [ ] Notificar nuevo seguidor
- [ ] Notificar cuando alguien comparte tu post
- [ ] Endpoint para obtener notificaciones

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
