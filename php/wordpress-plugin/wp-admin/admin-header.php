***REMOVED***
/**
 * WordPress Administration Template Header
 *
 * @package WordPress
 * @subpackage Administration
 */

@header('Content-Type: ' . get_option('html_type') . '; charset=' . get_option('blog_charset'));
if ( ! defined( 'WP_ADMIN' ) )
	require_once( dirname( __FILE__ ) . '/admin.php' );

// In case admin-header.php is included in a function.
global $title, $hook_suffix, $current_screen, $wp_locale, $pagenow, $wp_version,
	$current_site, $update_title, $total_update_count, $parent_file;

// Catch plugins that include admin-header.php before admin.php completes.
if ( empty( $current_screen ) )
	set_current_screen();

get_admin_page_title();
$title = esc_html( strip_tags( $title ) );

if ( is_network_admin() )
	$admin_title = sprintf( __('Network Admin: %s'), esc_html( $current_site->site_name ) );
elseif ( is_user_admin() )
	$admin_title = sprintf( __('Global Dashboard: %s'), esc_html( $current_site->site_name ) );
else
	$admin_title = get_bloginfo( 'name' );

if ( $admin_title == $title )
	$admin_title = sprintf( __( '%1$s &#8212; WordPress' ), $title );
else
	$admin_title = sprintf( __( '%1$s &lsaquo; %2$s &#8212; WordPress' ), $title, $admin_title );

/**
 * Filter the <title> content for an admin page.
 *
 * @since 3.1.0
 *
 * @param string $admin_title The page title, with extra context added.
 * @param string $title       The original page title.
 */
$admin_title = apply_filters( 'admin_title', $admin_title, $title );

wp_user_settings();

_wp_admin_html_begin();
?>
<title>***REMOVED*** echo $admin_title; ?></title>
***REMOVED***

wp_enqueue_style( 'colors' );
wp_enqueue_style( 'ie' );
wp_enqueue_script('utils');

$admin_body_class = preg_replace('/[^a-z0-9_-]+/i', '-', $hook_suffix);
?>
<script type="text/javascript">
addLoadEvent = function(func){if(typeof jQuery!="undefined")jQuery(document).ready(func);else if(typeof wpOnload!='function'){wpOnload=func;}else{var oldonload=wpOnload;wpOnload=function(){oldonload();func();}}};
var ajaxurl = '***REMOVED*** echo admin_url( 'admin-ajax.php', 'relative' ); ?>',
	pagenow = '***REMOVED*** echo $current_screen->id; ?>',
	typenow = '***REMOVED*** echo $current_screen->post_type; ?>',
	adminpage = '***REMOVED*** echo $admin_body_class; ?>',
	thousandsSeparator = '***REMOVED*** echo addslashes( $wp_locale->number_format['thousands_sep'] ); ?>',
	decimalPoint = '***REMOVED*** echo addslashes( $wp_locale->number_format['decimal_point'] ); ?>',
	isRtl = ***REMOVED*** echo (int) is_rtl(); ?>;
</script>
***REMOVED***

/**
 * Enqueue scripts for all admin pages.
 *
 * @since 2.8.0
 *
 * @param string $hook_suffix The current admin page.
 */
do_action( 'admin_enqueue_scripts', $hook_suffix );

/**
 * Print styles for a specific admin page based on $hook_suffix.
 *
 * @since 2.6.0
 */
do_action( "admin_print_styles-$hook_suffix" );

/**
 * Print styles for all admin pages.
 *
 * @since 2.6.0
 */
do_action( 'admin_print_styles' );

/**
 * Print scripts for a specific admin page based on $hook_suffix.
 *
 * @since 2.1.0
 */
do_action( "admin_print_scripts-$hook_suffix" );

/**
 * Print scripts for all admin pages.
 *
 * @since 2.1.0
 */
do_action( 'admin_print_scripts' );

/**
 * Fires in <head> for a specific admin page based on $hook_suffix.
 *
 * @since 2.1.0
 */
do_action( "admin_head-$hook_suffix" );

/**
 * Fires in <head> for all admin pages.
 *
 * @since 2.1.0
 */
do_action( 'admin_head' );

if ( get_user_setting('mfold') == 'f' )
	$admin_body_class .= ' folded';

if ( !get_user_setting('unfold') )
	$admin_body_class .= ' auto-fold';

if ( is_admin_bar_showing() )
	$admin_body_class .= ' admin-bar';

if ( is_rtl() )
	$admin_body_class .= ' rtl';

if ( $current_screen->post_type )
	$admin_body_class .= ' post-type-' . $current_screen->post_type;

if ( $current_screen->taxonomy )
	$admin_body_class .= ' taxonomy-' . $current_screen->taxonomy;

$admin_body_class .= ' branch-' . str_replace( array( '.', ',' ), '-', floatval( $wp_version ) );
$admin_body_class .= ' version-' . str_replace( '.', '-', preg_replace( '/^([.0-9]+).*/', '$1', $wp_version ) );
$admin_body_class .= ' admin-color-' . sanitize_html_class( get_user_option( 'admin_color' ), 'fresh' );
$admin_body_class .= ' locale-' . sanitize_html_class( strtolower( str_replace( '_', '-', get_locale() ) ) );

if ( wp_is_mobile() )
	$admin_body_class .= ' mobile';

$admin_body_class .= ' no-customize-support';

?>
</head>
***REMOVED***
/**
 * Filter the admin <body> CSS classes.
 *
 * This filter differs from the post_class or body_class filters in two important ways:
 * 1. $classes is a space-separated string of class names instead of an array.
 * 2. Not all core admin classes are filterable, notably: wp-admin, wp-core-ui, and no-js cannot be removed.
 *
 * @since 2.3.0
 *
 * @param string $classes Space-separated string of CSS classes.
 */
?>
<body class="wp-admin wp-core-ui no-js ***REMOVED*** echo apply_filters( 'admin_body_class', '' ) . " $admin_body_class"; ?>">
<script type="text/javascript">
	document.body.className = document.body.className.replace('no-js','js');
</script>

***REMOVED***
// Make sure the customize body classes are correct as early as possible.
if ( current_user_can( 'edit_theme_options' ) )
	wp_customize_support_script();
?>

<div id="wpwrap">
<a tabindex="1" href="#wpbody-content" class="screen-reader-shortcut">***REMOVED*** _e('Skip to main content'); ?></a>
***REMOVED*** require(ABSPATH . 'wp-admin/menu-header.php'); ?>
<div id="wpcontent">

***REMOVED***
/**
 * Fires at the beginning of the content section in an admin page.
 *
 * @since 3.0.0
 */
do_action( 'in_admin_header' );
?>

<div id="wpbody">
***REMOVED***
unset($title_class, $blog_name, $total_update_count, $update_title);

$current_screen->set_parentage( $parent_file );

?>

<div id="wpbody-content" aria-label="***REMOVED*** esc_attr_e('Main content'); ?>" tabindex="0">
***REMOVED***

$current_screen->render_screen_meta();

if ( is_network_admin() ) {
	/**
	 * Print network admin screen notices.
	 *
	 * @since 3.1.0
	 */
	do_action( 'network_admin_notices' );
} elseif ( is_user_admin() ) {
	/**
	 * Print user admin screen notices.
	 *
	 * @since 3.1.0
	 */
	do_action( 'user_admin_notices' );
} else {
	/**
	 * Print admin screen notices.
	 *
	 * @since 3.1.0
	 */
	do_action( 'admin_notices' );
}

/**
 * Print generic admin screen notices.
 *
 * @since 3.1.0
 */
do_action( 'all_admin_notices' );

if ( $parent_file == 'options-general.php' )
	require(ABSPATH . 'wp-admin/options-head.php');